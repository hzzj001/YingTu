#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
影图+（YingTu）quality-p0 静态核对脚本（第 6 步：验证）

只读：只读取仓库文件与 `git status`（不改源码、不联网、不写任何文件，
唯一的写出是同目录下的 VERIFY_STATIC.md，由本脚本生成）。

核对内容：
  1. 全部 Java 文件的 import 与全限定引用，对照仓库内已定义类，
     列出仍未解决的引用（类全名 + 引用文件数）；另附类型位置的
     “疑似未解析”清单（需人工复核：缺类 vs 缺 import）。
  2. 全部新增/修改 Java 文件的括号/花括号/方括号平衡（去字符串与注释后统计）。
  3. MISSING_CLASSES.md 声称已恢复/已兼容的类是否真实存在
     （恢复类全查 + 兼容类全查，另随机抽 20 个兼容类单列）。
  4. versionCode / versionName / targetSdk / compileSdk。
  5. 附带：R.layout.* 存在性、R.id.bottom、§6 遗留项（PlaybackActivity、
     activity_video.xml、ActivityVideoBinding）。

用法：
    python3 verify_static.py [--build-summary PATH]
"""
import os
import random
import re
import subprocess
import sys
import zipfile
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent
JAVA_HOME = Path("/usr/lib/jvm/java-17-openjdk-amd64")
ANDROID_JAR = Path("/opt/android-sdk/platforms/android-34/android.jar")

# ---------------------------------------------------------------- 工具函数

def strip_code(text):
    """去掉 Java 字符串（含文本块）、字符字面量、行注释与块注释。
    被去掉的部分用空格替换（保留换行），避免括号统计与引用扫描误报。"""
    out = []
    i, n = 0, len(text)
    while i < n:
        if text.startswith('"""', i):  # 文本块
            j = text.find('"""', i + 3)
            i = n if j == -1 else j + 3
            out.append(' ')
        elif text[i] == '"':
            j = i + 1
            while j < n:
                if text[j] == '\\':
                    j += 2
                    continue
                if text[j] in '"\n':
                    break
                j += 1
            i = min(j + 1, n)
            out.append(' ')
        elif text[i] == "'":
            j = i + 1
            while j < n:
                if text[j] == '\\':
                    j += 2
                    continue
                if text[j] in "'\n":
                    break
                j += 1
            i = min(j + 1, n)
            out.append(' ')
        elif text.startswith('//', i):
            j = text.find('\n', i)
            i = n if j == -1 else j
            out.append('\n')
        elif text.startswith('/*', i):
            j = text.find('*/', i + 2)
            i = n if j == -1 else j + 2
            out.append(' ')
        else:
            out.append(text[i])
            i += 1
    return ''.join(out)


TYPE_DECL_RE = re.compile(
    r'(?<![\w$.])'
    r'(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed|strictfp)\s+)*'
    r'(?:@interface|class|interface|enum|record)\s+([A-Z]\w*)'
)
PACKAGE_RE = re.compile(r'(?m)^\s*package\s+([\w.]+)\s*;')
IMPORT_RE = re.compile(r'(?m)^\s*import\s+(static\s+)?([\w.]+)(\.\*)?\s*;')

JAVA_LANG = {
    'String', 'Object', 'Integer', 'Long', 'Boolean', 'Double', 'Float',
    'Character', 'Byte', 'Short', 'Void', 'Math', 'System', 'Thread',
    'Runnable', 'Exception', 'RuntimeException', 'Error', 'Throwable',
    'Class', 'Enum', 'StringBuilder', 'StringBuffer', 'Iterable',
    'Comparable', 'Override', 'Deprecated', 'SuppressWarnings', 'FunctionalInterface',
    'Cloneable', 'Number', 'CharSequence', 'Appendable', 'AutoCloseable',
    'IllegalArgumentException', 'IllegalStateException', 'NullPointerException',
    'IndexOutOfBoundsException', 'UnsupportedOperationException',
    'InterruptedException', 'NoSuchElementException', 'ClassCastException',
    'SecurityException', 'ArithmeticException', 'ArrayIndexOutOfBoundsException',
    'StringIndexOutOfBoundsException', 'ClassNotFoundException',
    'AssertionError', 'NoSuchMethodError', 'NoSuchFieldError',
    'AbstractMethodError', 'UnsatisfiedLinkError', 'ExceptionInInitializerError',
    'OutOfMemoryError', 'StackOverflowError', 'NoClassDefFoundError',
    'InternalError', 'UnknownError', 'LinkageError', 'VirtualMachineError',
    'Runtime', 'ThreadLocal', 'InheritableThreadLocal', 'InstantiationException',
    'InvocationTargetException', 'CloneNotSupportedException',
    'IllegalAccessException', 'ReflectiveOperationException',
    'NoSuchFieldException', 'NoSuchMethodException',
}

GENERATED_SUFFIXES = ('.R', '.BuildConfig')

def brace_depths(code):
    """返回每个字符位置处的花括号深度（在该字符之前已有多少个未闭合 '{'）。"""
    depths = [0] * (len(code) + 1)
    d = 0
    for i, ch in enumerate(code):
        depths[i] = d
        if ch == '{':
            d += 1
        elif ch == '}':
            d -= 1
    depths[len(code)] = d
    return depths


def extract_defined(code, package):
    """提取一个文件内定义的全部类型。
    返回 (defined_fqns, all_simple_names, top_level_simple_names)。
    嵌套类型用括号深度栈还原全限定名。"""
    depths = brace_depths(code)
    defined = set()
    all_simple = set()
    top_level = set()
    stack = []  # (name, body_depth)
    for m in TYPE_DECL_RE.finditer(code):
        name = m.group(1)
        d = depths[m.start()]
        while stack and stack[-1][1] > d:
            stack.pop()
        parents = [s[0] for s in stack]
        fqn = '.'.join([package] + parents + [name]) if package else '.'.join(parents + [name])
        defined.add(fqn)
        all_simple.add(name)
        if not parents:
            top_level.add(name)
        # 找到类型体的左花括号，确定体深度
        j = code.find('{', m.end())
        semi = code.find(';', m.end())
        if j != -1 and (semi == -1 or j < semi):
            stack.append((name, depths[j] + 1))
    return defined, all_simple, top_level


def android_framework_classes():
    """从 android.jar 列出框架类：{包名: {类简单名}}。jar 不存在则返回 None。"""
    if not ANDROID_JAR.is_file():
        return None
    mapping = defaultdict(set)
    with zipfile.ZipFile(ANDROID_JAR) as z:
        for entry in z.namelist():
            if not entry.endswith('.class') or '$' in entry:
                continue
            path = entry[:-len('.class')]
            if '/' not in path:
                continue
            pkg, simple = path.rsplit('/', 1)
            mapping[pkg.replace('/', '.')].add(simple)
    return mapping


def load_android_jar():
    try:
        return android_framework_classes()
    except Exception as exc:  # noqa: BLE001 - 仅记录，不中断核对
        print(f"警告：读取 android.jar 失败（{exc}），Android 星号导入将无法判定", file=sys.stderr)
        return None

# ---------------------------------------------------------------- 收集仓库

def collect_java_files():
    files = sorted(
        p for p in ROOT.rglob('*.java')
        if '.git' not in p.parts
    )
    return files


def git_changed_java_files():
    """新增/修改的 Java 文件（git status --porcelain）。失败则返回 None。"""
    try:
        out = subprocess.run(
            ['git', '-C', str(ROOT), 'status', '--porcelain', '--', '*.java'],
            capture_output=True, text=True, timeout=60,
        )
    except Exception:
        return None
    if out.returncode != 0:
        return None
    changed = []
    for line in out.stdout.splitlines():
        if len(line) < 4:
            continue
        path = line[3:].strip()
        if ' -> ' in path:  # 重命名取新路径
            path = path.split(' -> ', 1)[1]
        p = ROOT / path
        if p.suffix == '.java' and p.is_file():
            changed.append(p)
    return sorted(changed)


def git_info():
    def run(*args):
        try:
            out = subprocess.run(['git', '-C', str(ROOT), *args],
                                 capture_output=True, text=True, timeout=30)
            return out.stdout.strip()
        except Exception:
            return ''
    return run('branch', '--show-current'), run('rev-parse', '--short', 'HEAD')


# ---------------------------------------------------------------- 引用核对

def is_generated(fqn):
    return fqn.endswith(GENERATED_SUFFIXES) or '.databinding.' in fqn


def package_is_in_repo(pkg, defined_packages):
    """包是否属于仓库内包：等于已定义包、是已定义包的子包、或是已定义包的父包。"""
    if not pkg:
        return False
    for p in defined_packages:
        if pkg == p or pkg.startswith(p + '.') or p.startswith(pkg + '.'):
            return True
    return False


def check_references(java_files):
    """返回：defined_fqns, defined_packages, in_repo_roots,
    unresolved_imports {fqn: [文件]}, unresolved_fq {fqn: [文件]},
    suspect_types {token: [文件]}, 统计字典。"""
    defined_fqns = set()
    defined_packages = set()
    pkg_top_level = defaultdict(set)      # 包 -> 顶层类简单名
    file_info = {}                        # path -> dict
    for path in java_files:
        text = path.read_text(encoding='utf-8', errors='replace')
        code = strip_code(text)
        m = PACKAGE_RE.search(code)
        package = m.group(1) if m else ''
        defined, all_simple, top_level = extract_defined(code, package)
        defined_fqns |= defined
        if package:
            defined_packages.add(package)
            pkg_top_level[package] |= top_level
        imports = []       # (fqn, is_static, is_star)
        star_imports = []  # 包名
        for im in IMPORT_RE.finditer(code):
            is_static = bool(im.group(1))
            fqn = im.group(2)
            is_star = bool(im.group(3))
            if is_star:
                star_imports.append(fqn)
            else:
                imports.append((fqn, is_static))
        file_info[path] = {
            'package': package, 'code': code, 'defined': defined,
            'all_simple': all_simple, 'imports': imports,
            'star_imports': star_imports,
            'imported_simple': {f.rsplit('.', 1)[-1] for f, _ in imports},
        }

    # 仓库内根包：已定义包的极小前缀集
    roots = set()
    for p in defined_packages:
        if not any(q != p and p.startswith(q + '.') for q in defined_packages):
            roots.add(p)
    root_re = re.compile(
        r'(?<![\w$.])((?:' + '|'.join(re.escape(r) for r in sorted(roots)) +
        r')(?:\.[\w$]+)+)'
    ) if roots else None

    android_map = load_android_jar()

    unresolved_imports = defaultdict(list)
    unresolved_fq = defaultdict(list)
    suspect_types = defaultdict(list)
    stats = Counter()

    for path, info in file_info.items():
        code = info['code']
        package = info['package']
        # --- import ---
        for fqn, is_static in info['imports']:
            stats['imports'] += 1
            if is_generated(fqn):
                stats['imports_generated'] += 1
                continue
            if fqn in defined_fqns:
                continue
            check_fqn = fqn.rsplit('.', 1)[0] if is_static else fqn
            if check_fqn in defined_fqns:
                continue
            pkg = check_fqn.rsplit('.', 1)[0] if '.' in check_fqn else ''
            if package_is_in_repo(pkg, defined_packages):
                unresolved_imports[check_fqn].append(path)
            else:
                stats['imports_external'] += 1
        # --- 全限定引用（去掉 import 行） ---
        code_no_import = re.sub(r'(?m)^\s*import\s+[\w.$*]+\s*;\s*$', '', code)
        if root_re:
            for m in root_re.finditer(code_no_import):
                chain = m.group(1)
                stats['fq_refs'] += 1
                segments = chain.split('.')
                cls_idx = next((i for i, s in enumerate(segments)
                                if s and s[0].isupper()), None)
                if cls_idx is None:
                    continue
                fqn = '.'.join(segments[:cls_idx + 1])
                if is_generated(fqn):
                    stats['fq_generated'] += 1
                    continue
                if fqn in defined_fqns:
                    continue
                unresolved_fq[fqn].append(path)
        # --- 类型位置疑似引用（次要清单，需人工复核） ---
        # 强信号：new / extends / implements / throws / instanceof / 注解 /
        #        泛型实参 / 强制转换 / Xxx.yyy 的限定符。
        # 弱信号：方法调用里逗号后的裸标识（可能是泛型实参，也可能是枚举常量实参）。
        # 另：Outer.Inner 形式中，若 Outer 已解析，则 Inner 视为 Outer 的嵌套类型而解析
        #     （Map.Entry、RecyclerView.Adapter、PlaybackAutoContext.Fact 等均属此类，
        #      否则会把所有嵌套类型误报为缺失类）。
        strong = set()
        weak = set()
        for pat in (
            r'\bnew\s+([A-Z]\w*)',
            r'\bextends\s+([A-Z]\w*)',
            r'\bthrows\s+([A-Z]\w*(?:\s*,\s*[A-Z]\w*)*)',
            r'\binstanceof\s+([A-Z]\w*)',
            r'@([A-Z]\w*)',
            r'<\s*\??\s*(?:extends\s+|super\s+)?([A-Z]\w*)(?=\s*[>,])',
            r',\s*([A-Z]\w*)\s*>',
        ):
            for m in re.finditer(pat, code):
                for g in m.groups():
                    if g:
                        strong.update(t.strip() for t in g.split(',') if t.strip())
        for m in re.finditer(r'\bimplements\s+([A-Z]\w*(?:\s*,\s*[A-Z]\w*)*)', code):
            strong.update(t.strip() for t in m.group(1).split(',') if t.strip())
        for m in re.finditer(r'(?<![\w$.\)\]])\(\s*([A-Z]\w*)\s*\)(?=\s*[\w(.])', code):
            strong.add(m.group(1))
        for m in re.finditer(r'(?<![\w$.])([A-Z]\w*)\s*\.\s*[\w$]+', code):
            # 全大写限定符是常量/字段（如 EDGE_SEPARATORS.matcher(..)），不是类型
            if re.fullmatch(r'[A-Z][A-Z0-9_]*', m.group(1)):
                continue
            strong.add(m.group(1))
        # 泛型外层类型：Foo<Bar> 的 Foo；要求 < 后是类型（大写或 ?），
        # 排除 elapsedMs < timeout 之类的数值比较。
        for m in re.finditer(r'(?<![\w$.])([A-Z]\w*)\s*<(?=\s*[A-Z?])', code):
            strong.add(m.group(1))
        for m in re.finditer(r',\s*([A-Z]\w*)\s*,', code):
            weak.add(m.group(1))
        # 嵌套类型解析（迭代至不动点）：Outer.Inner 中 Outer 已解析 -> Inner 已解析
        def outer_resolves(outer):
            if outer in info['all_simple']:
                return True
            if outer in pkg_top_level.get(package, set()):
                return True
            if outer in info['imported_simple']:
                return True
            if outer in JAVA_LANG:
                return True
            for sp in info['star_imports']:
                if android_map is not None and sp in android_map \
                        and outer in android_map[sp]:
                    return True
                if outer in pkg_top_level.get(sp, set()):
                    return True
            return False

        nested_resolved = set()
        for _ in range(3):
            added = False
            for m in re.finditer(r'(?<![\w$.])([A-Z]\w*)\.([A-Z]\w*)', code):
                outer, inner = m.group(1), m.group(2)
                if inner in nested_resolved:
                    continue
                if outer_resolves(outer) or outer in nested_resolved:
                    nested_resolved.add(inner)
                    added = True
            if not added:
                break
        tokens = strong | weak
        for tok in tokens:
            if tok in ('R',) or len(tok) == 1:
                continue
            if tok in nested_resolved:
                continue
            if tok in info['all_simple']:
                continue
            if tok in pkg_top_level.get(package, set()):
                continue
            if tok in info['imported_simple']:
                continue
            if tok in JAVA_LANG:
                continue
            # 全大写常量只在弱信号里出现时排除（如方法实参里的枚举常量）；
            # 强位置（new/extends 等）里的全大写仍保留。
            if tok in weak and tok not in strong \
                    and re.fullmatch(r'[A-Z][A-Z0-9_]*', tok):
                continue
            # 星号导入：仓库内包按已定义类判定；Android 包按 android.jar 判定
            resolved_by_star = False
            star_undecidable = False
            for sp in info['star_imports']:
                if sp in pkg_top_level or package_is_in_repo(sp, defined_packages):
                    if tok in pkg_top_level.get(sp, set()):
                        resolved_by_star = True
                elif android_map is not None and sp in android_map:
                    if tok in android_map[sp]:
                        resolved_by_star = True
                elif android_map is not None and sp.startswith('android.'):
                    resolved_by_star = False
                else:
                    star_undecidable = True
            if resolved_by_star or star_undecidable:
                continue
            suspect_types[tok].append(path)

    return {
        'defined_fqns': defined_fqns, 'defined_packages': defined_packages,
        'roots': roots, 'unresolved_imports': dict(unresolved_imports),
        'unresolved_fq': dict(unresolved_fq), 'suspect_types': dict(suspect_types),
        'stats': stats, 'file_info': file_info,
    }

# ---------------------------------------------------------------- 括号平衡

def check_braces(paths):
    """返回 {path: {'(': n, ')': n, '{': n, '}': n, '[': n, ']': n}}（仅列出不平衡的）。"""
    bad = {}
    for path in paths:
        text = path.read_text(encoding='utf-8', errors='replace')
        code = strip_code(text)
        counts = {c: code.count(c) for c in '(){}[]'}
        if counts['('] != counts[')'] or counts['{'] != counts['}'] or counts['['] != counts[']']:
            bad[path] = counts
    return bad


# ---------------------------------------------------------------- MISSING_CLASSES.md 兑现检查

def parse_missing_classes():
    """解析 MISSING_CLASSES.md §2（恢复类）与 §3（兼容类）。
    返回 (restored, compatible)，元素为 dict(name=..., kind=..., hint=..., raw=...)。"""
    text = (ROOT / 'MISSING_CLASSES.md').read_text(encoding='utf-8', errors='replace')
    lines = text.splitlines()
    section = None
    section_hint = ''
    restored, compatible = [], []
    for line in lines:
        if line.startswith('## 2.'):
            section = 'restored'
            continue
        if line.startswith('## 3.'):
            section = 'compatible'
            continue
        if line.startswith('## 4.'):
            section = None
            continue
        if section == 'compatible' and line.startswith('### '):
            hm = re.findall(r'`([^`]+)`', line)
            section_hint = hm[0] if hm else ''
            continue
        if section and line.strip().startswith('|') and '`' in line:
            cells = [c.strip() for c in line.strip().strip('|').split('|')]
            if not cells or cells[0].startswith('---') or cells[0] in ('类', '项'):
                continue
            raw = cells[0].strip('`').strip()
            if not raw or raw in ('类',):
                continue
            hint = section_hint
            name = raw
            if '（' in raw:
                name, hint = raw.split('（', 1)
                hint = hint.rstrip('）').strip('`').strip()
                name = name.strip('`').strip()
            item = {'name': name, 'hint': hint, 'raw': raw}
            if section == 'restored':
                restored.append(item)
            else:
                compatible.append(item)
    return restored, compatible


def find_java_file(name, hint):
    """按名字/包提示在仓库中找 Java 文件。返回 (是否找到, 找到的路径或 None)。"""
    # 全限定名
    if re.match(r'^([a-z][\w]*\.)+[A-Z]\w*$', name):
        rel = name.replace('.', '/') + '.java'
        for base in ('app/src/main/java', 'catvod/src/main/java',
                     'quickjs/src/main/java'):
            p = ROOT / base / rel
            if p.is_file():
                return True, p
        # 兜底：按文件名搜
        simple = name.rsplit('.', 1)[-1]
        cands = [p for p in ROOT.rglob(simple + '.java') if '.git' not in p.parts]
        return bool(cands), (cands[0] if cands else None)
    # 相对路径（可能不带 .java）
    if '/' in name:
        rel = name if name.endswith('.java') else name + '.java'
        for base in ('app/src/main/java/com/yingtu/plus',
                     'app/src/main/java', 'catvod/src/main/java'):
            p = ROOT / base / rel
            if p.is_file():
                return True, p
        simple = rel.rsplit('/', 1)[-1]
        cands = [p for p in ROOT.rglob(simple) if '.git' not in p.parts]
        if hint:
            norm = hint.strip('`').replace('\\', '/')
            for p in cands:
                if norm in p.as_posix():
                    return True, p
        return bool(cands), (cands[0] if cands else None)
    # 裸类名
    simple = name
    cands = [p for p in ROOT.rglob(simple + '.java') if '.git' not in p.parts]
    if hint:
        norm = hint.strip('`').replace('\\', '/')
        for p in cands:
            if norm in p.as_posix():
                return True, p
        return False, (cands[0] if cands else None)
    return bool(cands), (cands[0] if cands else None)


def check_missing_classes():
    restored, compatible = parse_missing_classes()
    res = {'restored': [], 'compatible': [], 'restored_missing': [],
           'compatible_missing': []}
    for item in restored:
        ok, _ = find_java_file(item['name'], item['hint'])
        res['restored'].append((item, ok))
        if not ok:
            res['restored_missing'].append(item)
    for item in compatible:
        ok, _ = find_java_file(item['name'], item['hint'])
        res['compatible'].append((item, ok))
        if not ok:
            res['compatible_missing'].append(item)
    rng = random.Random(20261108)
    sample = rng.sample(res['compatible'], min(20, len(res['compatible']))) \
        if res['compatible'] else []
    res['sample20'] = sample
    return res


# ---------------------------------------------------------------- 版本与资源

def check_versions():
    out = {}
    gradle = ROOT / 'app/build.gradle'
    text = gradle.read_text(encoding='utf-8', errors='replace')
    for key, pat in (('compileSdk', r'compileSdk\s+(\d+)'),
                     ('targetSdk', r'targetSdk\s+(\d+)'),
                     ('minSdk', r'minSdk\s+(\d+)'),
                     ('versionCode', r'versionCode\s+(\d+)'),
                     ('versionName', r'versionName\s+"([^"]+)"')):
        m = re.search(pat, text)
        out[key] = m.group(1) if m else None
    for mod in ('catvod', 'quickjs'):
        f = ROOT / mod / 'build.gradle'
        if f.is_file():
            t = f.read_text(encoding='utf-8', errors='replace')
            m = re.search(r'compileSdk\s+(\d+)', t)
            out[mod + '_compileSdk'] = m.group(1) if m else None
    root_gradle = (ROOT / 'build.gradle').read_text(encoding='utf-8', errors='replace')
    m = re.search(r"com\.android\.tools\.build:gradle:([0-9.]+)", root_gradle)
    out['agp'] = m.group(1) if m else None
    return out


def check_resources(file_info):
    layouts = {p.stem for p in (ROOT / 'app/src/main/res/layout').glob('*.xml')}
    layout_refs = Counter()
    id_bottom = 0
    binding_refs = Counter()
    for path, info in file_info.items():
        code = info['code']
        for m in re.finditer(r'\bR\.layout\.([A-Za-z0-9_]+)', code):
            layout_refs[m.group(1)] += 1
        id_bottom += len(re.findall(r'\bR\.id\.bottom\b', code))
        for m in re.finditer(r'\b(\w+Binding)\b', code):
            binding_refs[m.group(1)] += 1
    missing_layouts = {name: n for name, n in layout_refs.items() if name not in layouts}
    return layouts, layout_refs, missing_layouts, id_bottom, binding_refs


def check_remaining():
    """§6 遗留项：PlaybackActivity、activity_video.xml、ActivityVideoBinding 引用。"""
    pa = [p for p in ROOT.rglob('PlaybackActivity.java') if '.git' not in p.parts]
    av = list((ROOT / 'app/src/main/res/layout').glob('activity_video.xml'))
    binding_ref_files = []
    for p in ROOT.rglob('*.java'):
        if '.git' in p.parts:
            continue
        text = p.read_text(encoding='utf-8', errors='replace')
        if 'ActivityVideoBinding' in strip_code(text):
            binding_ref_files.append(p)
    key = ''
    store = ROOT / 'app/src/main/java/com/yingtu/plus/web/WebHomeInlineVodStore.java'
    if store.is_file():
        m = re.search(r'KEY\s*=\s*"([^"]*)"',
                      store.read_text(encoding='utf-8', errors='replace'))
        key = m.group(1) if m else '(未找到 KEY 定义)'
    return pa, av, binding_ref_files, key

# ---------------------------------------------------------------- 报告

def rel(p):
    try:
        return str(Path(p).relative_to(ROOT))
    except ValueError:
        return str(p)


def short_list(paths, limit=5):
    shown = [rel(p) for p in sorted(paths)[:limit]]
    more = len(paths) - len(shown)
    return '、'.join(shown) + (f' 等（共 {len(paths)} 个文件）' if more > 0 else '')


def main():
    import argparse
    ap = argparse.ArgumentParser(description='影图+ quality-p0 静态核对')
    ap.add_argument('--build-summary', default=None, help='编译尝试摘要文本文件')
    args = ap.parse_args()

    import datetime
    now = datetime.datetime.now(datetime.timezone.utc).astimezone(
        datetime.timezone(datetime.timedelta(hours=8)))
    branch, commit = git_info()
    java_files = collect_java_files()
    changed = git_changed_java_files()
    ref = check_references(java_files)
    stats = ref['stats']
    brace_changed = check_braces(changed) if changed is not None else {}
    brace_all = check_braces(java_files)
    mc = check_missing_classes()
    versions = check_versions()
    layouts, layout_refs, missing_layouts, id_bottom, binding_refs = \
        check_resources(ref['file_info'])
    pa, av, binding_ref_files, vod_key = check_remaining()
    build_summary = ''
    if args.build_summary and Path(args.build_summary).is_file():
        build_summary = Path(args.build_summary).read_text(encoding='utf-8')

    L = []
    L.append('# 影图+（YingTu）quality-p0 静态核对报告（第 6 步：验证）\n')
    L.append(f'- 核对时间：{now.strftime("%Y-%m-%d %H:%M")}（北京时间，UTC+8）')
    L.append(f'- 分支：`{branch}`，HEAD：`{commit}`')
    L.append(f'- 核对脚本：`verify_static.py`（只读仓库文件，不改源码）')
    L.append(f'- Java 文件总数：{len(java_files)} 个；已定义类型全限定名：'
             f'{len(ref["defined_fqns"])} 个；仓库内包：{len(ref["defined_packages"])} 个')
    L.append('- 结论先行：真实 Gradle 编译未完成（本机 Gradle daemon 无法通信，见 §0），'
             '以下均为静态核对结果，不能替代编译验证。\n')

    L.append('## 0. 编译尝试摘要\n')
    if build_summary:
        L.append(build_summary.strip() + '\n')
    else:
        L.append('（本次运行未传入编译摘要）\n')

    # ---- §1 引用核对 ----
    L.append('## 1. 引用核对\n')
    L.append(f'- import 总数：{stats["imports"]}（其中构建生成类 R/BuildConfig/databinding：'
             f'{stats["imports_generated"]}；外部依赖：{stats["imports_external"]}）')
    L.append(f'- 全限定引用扫描命中：{stats["fq_refs"]} 处（构建生成类：'
             f'{stats["fq_generated"]}）\n')
    ui = ref['unresolved_imports']
    uf = ref['unresolved_fq']
    L.append(f'### 1.1 import 未解决：{len(ui)} 个类\n')
    if ui:
        L.append('| 类全名 | 引用文件数 | 引用文件（示例） |')
        L.append('|---|---|---|')
        for fqn in sorted(ui):
            files = sorted(set(ui[fqn]))
            L.append(f'| `{fqn}` | {len(files)} | {short_list(files)} |')
        L.append('')
    else:
        L.append('无。全部 import 的仓库内类均已定义。\n')
    L.append(f'### 1.2 全限定引用未解决：{len(uf)} 个类\n')
    if uf:
        L.append('| 类全名 | 引用文件数 | 引用文件（示例） |')
        L.append('|---|---|---|')
        for fqn in sorted(uf):
            files = sorted(set(uf[fqn]))
            L.append(f'| `{fqn}` | {len(files)} | {short_list(files)} |')
        L.append('')
    else:
        L.append('无。全部全限定引用的仓库内类均已定义。\n')
    st = ref['suspect_types']
    L.append(f'### 1.3 疑似未解析的类型位置引用：{len(st)} 个标识符（需人工复核）\n')
    L.append('口径：出现在 `new` / `extends` / `implements` / `throws` / `instanceof` / '
             '注解 / 泛型 / 强制转换 / `Xxx.yyy` 限定符位置的大写标识符，在本文件内既无定义、'
             '也不属于同包类、import、java.lang、星号导入（Android 框架按 android.jar 判定，'
             '其他外部星号导入按“无法判定”排除）。已做两项去噪：`Outer.Inner` 中 Outer 已解析时 '
             'Inner 按嵌套类型解析（Map.Entry、RecyclerView.Adapter、'
             'PlaybackAutoContext.Fact 等均属此类，不再误报）；方法实参里仅以逗号分隔出现的全大写'
             '标识按枚举常量排除。此类信号可能是缺类，也可能是缺 import，仍可能有少量误报，'
             '须逐项人工确认，不直接计入 1.1/1.2。\n')
    if st:
        L.append('| 标识符 | 引用文件数 | 引用文件（示例） |')
        L.append('|---|---|---|')
        for tok in sorted(st):
            files = sorted(set(st[tok]))
            L.append(f'| `{tok}` | {len(files)} | {short_list(files)} |')
        L.append('')
    else:
        L.append('无。\n')

    # ---- §2 括号 ----
    L.append('## 2. 括号 / 花括号平衡检查（去字符串与注释后统计）\n')
    changed_n = len(changed) if changed is not None else 0
    L.append(f'- 新增/修改的 Java 文件：{changed_n} 个（`git status --porcelain`）')
    if changed is None:
        L.append('- 注意：git 状态读取失败，本节退化为全量检查。')
    if brace_changed:
        L.append(f'- 其中括号不平衡：{len(brace_changed)} 个（必须修）：')
        L.append('| 文件 | ( | ) | { | } | [ | ] |')
        L.append('|---|---|---|---|---|---|---|')
        for p in sorted(brace_changed):
            c = brace_changed[p]
            L.append(f"| `{rel(p)}` | {c['(']} | {c[')']} | {c['{']} | {c['}']} | "
                     f"{c['[']} | {c[']']} |")
        L.append('')
    else:
        L.append('- 其中括号不平衡：0 个。\n')
    extra = {p: c for p, c in brace_all.items() if p not in brace_changed}
    if extra:
        L.append(f'- 另：全量检查中除上述文件外还有 {len(extra)} 个不平衡文件（属旧文件，需复核）：')
        for p in sorted(extra):
            c = extra[p]
            L.append(f"  - `{rel(p)}`：() {c['(']}/{c[')']}，{{}} {c['{']}/{c['}']}，"
                     f"[] {c['[']}/{c[']']}")
        L.append('')
    else:
        L.append('- 全量检查：其余文件全部平衡。\n')

    # ---- §3 MISSING_CLASSES.md ----
    L.append('## 3. MISSING_CLASSES.md 兑现检查\n')
    r_ok = sum(1 for _, ok in mc['restored'] if ok)
    c_ok = sum(1 for _, ok in mc['compatible'] if ok)
    L.append(f'- §2 恢复类：{r_ok}/{len(mc["restored"])} 个存在。'
             + ('全部存在。' if not mc['restored_missing'] else '缺失见下表。'))
    if mc['restored_missing']:
        L.append('| 声称恢复的类 | 清单原文 |')
        L.append('|---|---|')
        for it in mc['restored_missing']:
            L.append(f"| `{it['name']}` | `{it['raw']}` |")
        L.append('')
    L.append(f'- §3 兼容类：{c_ok}/{len(mc["compatible"])} 个存在。'
             + ('全部存在。' if not mc['compatible_missing'] else '缺失见下表。'))
    if mc['compatible_missing']:
        L.append('| 声称兼容的类 | 清单原文 |')
        L.append('|---|---|')
        for it in mc['compatible_missing']:
            L.append(f"| `{it['name']}` | `{it['raw']}` |")
        L.append('')
    s_ok = sum(1 for it, ok in mc['sample20'] if ok)
    L.append(f'- 随机抽 20 个兼容类（种子 20261108）：{s_ok}/20 个存在：')
    for it, ok in mc['sample20']:
        L.append(f"  - `{it['name']}`（提示：{it['hint'] or '无'}）：{'存在' if ok else '缺失'}")
    L.append('')

    # ---- §4 版本 ----
    L.append('## 4. 版本检查（`app/build.gradle`）\n')
    L.append('| 项 | 期望 | 实际 | 结果 |')
    L.append('|---|---|---|---|')
    for key, expect in (('versionCode', '32'), ('versionName', '2.0.1'),
                        ('targetSdk', '34'), ('compileSdk', '34')):
        actual = versions.get(key)
        mark = '通过' if str(actual) == expect else '不通过'
        L.append(f'| {key} | {expect} | {actual} | {mark} |')
    L.append(f"| minSdk | — | {versions.get('minSdk')} | 仅记录 |")
    L.append(f"| AGP（根 build.gradle） | — | {versions.get('agp')} | 仅记录 |")
    for mod in ('catvod', 'quickjs'):
        L.append(f"| {mod} compileSdk | — | {versions.get(mod + '_compileSdk')} | 仅记录 |")
    L.append('')

    # ---- §5 资源与遗留 ----
    L.append('## 5. 资源与 §6 遗留项检查\n')
    L.append(f'- Java 引用的 `R.layout.*`：{len(layout_refs)} 个不同名字，'
             f'引用总计 {sum(layout_refs.values())} 处；'
             f'缺失的 layout：{len(missing_layouts)} 个。')
    if missing_layouts:
        for name, n in sorted(missing_layouts.items()):
            L.append(f'  - `R.layout.{name}`（{n} 处引用，无对应 xml）')
    L.append(f'- `R.id.bottom` 引用：{id_bottom} 处。')
    L.append(f'- `PlaybackActivity`（VideoActivity 基类）：'
             + ('仍缺失（未找到 PlaybackActivity.java）。' if not pa
                else f'已存在：{rel(pa[0])}。'))
    L.append(f'- `activity_video.xml`：'
             + ('仍缺失。' if not av else '已存在。'))
    L.append(f'- `ActivityVideoBinding` 引用：{len(binding_ref_files)} 个文件'
             f'（{short_list(binding_ref_files, 8) if binding_ref_files else "无"}）。')
    L.append(f'- `WebHomeInlineVodStore.KEY` 当前值：`{vod_key}`'
             '（清单第 6 节注明真实值树内无据，需上游对齐）。')
    L.append('')

    # ---- §6 结论 ----
    L.append('## 6. 结论与下一步建议\n')
    n_unres = len(ui) + len(uf)
    L.append(f'- 仍未解决的引用（import + 全限定）：{n_unres} 个类；'
             f'疑似类型位置引用：{len(st)} 个标识符（待人工复核）。')
    L.append('- 疑似清单人工复核结论（逐项查过引用行与所在文件 import）：'
             '除个别 java.lang 误报已在脚本白名单中排除外，其余基本属实，分两类——'
             '（a）缺 import：多为 media3/Android 嵌套类型被裸用，如 AudioSink 的 '
             'ConfigurationException/InitializationException/WriteException、'
             'VideoSink 的 Listener/VideoSinkException、Extractor 的 ReadResult、'
             'SeekMap 的 SeekPoints、BandwidthMeter 的 EventListener、Player 的 Commands、'
             'SimpleBasePlayer 的 State/PositionSupplier/MediaItemData、'
             'MediaCodecVideoRenderer 的 Builder、Parcelable 的 Creator（11 个 bean 文件）、'
             'Application 的 ActivityLifecycleCallbacks、androidx.annotation 的 '
             'NonNull/Nullable（2 个 dialog 文件）；'
             '（b）缺类：仓库内无定义、无 import，如 FolderActivity、'
             'EpisodeGridLayoutPolicy、ErrorAction、RuntimeMetrics、'
             'VideoPlaybackDetails、DiagnosticControls、ReadRequest，'
             '以及 IjkSimplePlayer 用的 PositionSupplier/MediaItemData'
             '（若属 media3 嵌套类型则归入（a），否则归入（b），需下一步对照 media3 版本核定）。'
             '本清单未覆盖的 ActivityVideoBinding 缺失见 §5/§6。')
    L.append('- 括号平衡：见 §2。')
    L.append('- 版本：见 §4。')
    L.append('- 最大阻塞仍是 §6 的两项：`PlaybackActivity` 缺失与 `activity_video.xml` '
             '缺失——VideoActivity（6619 行）直接继承前者、绑定后者，缺了这两项，'
             '任何编译都到不了链接阶段。')
    L.append('- 其次是环境阻塞：本机 Gradle daemon 无法通信（见 §0），编译验证本身还没跑通过；'
             '即使源码齐了，也需要在能跑 Gradle 的环境（或 CI）上再验证一次。')
    L.append('- 另：仓库自带的 `gradlew` 有引号缺陷（见 §0），在任何机器上直接跑都会失败，'
             '修编译验证前需先修它（或改用独立 Gradle）。')
    L.append('')
    L.append('> 本报告由 `verify_static.py` 生成；编译部分因环境原因未完成，'
             '静态核对不能替代真实编译。')

    out_path = ROOT / 'VERIFY_STATIC.md'
    out_path.write_text('\n'.join(L), encoding='utf-8')
    print(f'Java 文件：{len(java_files)} 个')
    print(f"import 未解决：{len(ui)} 个；全限定引用未解决：{len(uf)} 个；"
          f"疑似类型引用：{len(st)} 个")
    print(f"括号不平衡（新增/修改）：{len(brace_changed)} 个；全量另有：{len(extra)} 个")
    print(f"恢复类：{r_ok}/{len(mc['restored'])}；兼容类：{c_ok}/{len(mc['compatible'])}；"
          f"随机抽 20：{s_ok}/20")
    print(f"版本：versionCode={versions.get('versionCode')} "
          f"versionName={versions.get('versionName')} "
          f"targetSdk={versions.get('targetSdk')} compileSdk={versions.get('compileSdk')}")
    print(f"缺失 layout：{len(missing_layouts)} 个；R.id.bottom：{id_bottom} 处")
    print(f'报告已写到：{out_path}')


if __name__ == '__main__':
    main()
