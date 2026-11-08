package com.yingtu.plus.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.yingtu.plus.R;
import com.yingtu.plus.api.config.VodConfig;
import com.yingtu.plus.bean.Site;
import com.yingtu.plus.setting.Setting;
import com.yingtu.plus.ui.fragment.VodFragment;
import com.yingtu.plus.web.WebHomeChrome;
import com.yingtu.plus.web.WebHomeChromeStartup;
import com.yingtu.plus.web.WebHomeViewport;
import com.google.gson.JsonObject;
import com.yingtu.plus.api.ApiConfig;
import com.yingtu.plus.base.BaseActivity;
import com.yingtu.plus.bean.SourceBean;
import com.yingtu.plus.ui.adapter.HomePagerAdapter;
import com.yingtu.plus.ui.adapter.SourceGridAdapter;
import com.yingtu.plus.util.OkGo;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 首页外壳。顶部 Logo + 标题（当前源名）+ 日期，下方 Tab。
 * 点击标题/Logo 弹出站源选择小窗（带搜索 + 分类筛选）。
 */
public class HomeActivity extends BaseActivity {

    public static final String KEY_TAB = "tab";

    private TextView tvDate, tvTitle, tvSourceCount;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SourceGridAdapter sourceAdapter;
    private PopupWindow sourcePopup;
    private EditText etSearch;
    private LinearLayout chipContainer;

    private final Handler clock = new Handler(Looper.getMainLooper());

    // WebHome chrome 状态（按影图外壳适配，与上游行为有差异）：上游 HomeActivity 用 WebHomeChromeController
    //（Activity 级控制器，管 toolbar/视口/横竖屏）持有这些状态；影图外壳没有该控制器，故只用两个字段
    // 本地保存，VodFragment 通过下面一组方法读写；无 WebHome 状态时 viewport 恒为 WebHomeViewport.EMPTY。
    private String webHomeChromeMode = WebHomeChrome.NORMAL;
    private WebHomeViewport webHomeViewport = WebHomeViewport.EMPTY;
    private final String[] tabTitles = {"主页", "网页首页", "本地文件", "直链收藏", "分享收藏"};

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void init() {
        // 应用自定义壁纸
        applyCustomWallpaper();
        tvDate = find(R.id.tvDate);
        tvTitle = find(R.id.tvTitle);
        viewPager = find(R.id.viewPager);
        tabLayout = find(R.id.tabLayout);

        viewPager.setAdapter(new HomePagerAdapter(this));
        viewPager.setOffscreenPageLimit(tabTitles.length);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])).attach();

        int tab = getIntent().getIntExtra(KEY_TAB, 0);
        if (tab > 0 && tab < tabTitles.length) viewPager.setCurrentItem(tab, false);

        // 点击标题/Logo 打开站源弹窗
        tvTitle.setOnClickListener(v -> openSourcePopup());
        find(R.id.ivLogo).setOnClickListener(v -> openSourcePopup());

        setupSourcePopup();
        refreshTitle();
        startClock();
    }

    private void setupSourcePopup() {
        View popupView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_source_picker, null);

        sourcePopup = new PopupWindow(popupView,
                (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        sourcePopup.setOutsideTouchable(true);
        sourcePopup.setBackgroundDrawable(null);
        sourcePopup.setAnimationStyle(android.R.style.Animation_Dialog);

        etSearch = popupView.findViewById(R.id.etSearch);
        chipContainer = popupView.findViewById(R.id.chipContainer);
        tvSourceCount = popupView.findViewById(R.id.tvSourceCount);

        androidx.recyclerview.widget.RecyclerView rv = popupView.findViewById(R.id.rvSource);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        sourceAdapter = new SourceGridAdapter();
        sourceAdapter.setOnSourceClick(this::onSourcePicked);
        rv.setAdapter(sourceAdapter);

        // 构建 chip
        buildChips();

        // 搜索
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                sourceAdapter.setSearch(s.toString());
            }
        });

        // 管理源按钮
        popupView.findViewById(R.id.btnManageSource).setOnClickListener(v -> {
            if (sourcePopup.isShowing()) sourcePopup.dismiss();
            startActivity(new Intent(this, SourceActivity.class));
        });
    }

    private void buildChips() {
        chipContainer.removeAllViews();
        final List<String> chips = sourceAdapter.getChipList();
        final String currentChip = sourceAdapter.getFilterChip();
        for (int i = 0; i < chips.size(); i++) {
            final String chipText = chips.get(i);
            final boolean isChecked = chipText.equals(currentChip);
            Chip c = new Chip(this);
            c.setText(chipText);
            c.setCheckable(true);
            c.setChecked(isChecked);
            c.setOnClickListener(v -> {
                sourceAdapter.setFilterChip(chipText);
                // Rebuild chips to update checked states
                buildChips();
            });
            chipContainer.addView(c);
        }
    }

    private void openSourcePopup() {
        List<SourceBean> sources = ApiConfig.get().getSourceBeanList();
        if (sources.isEmpty()) {
            startActivity(new Intent(this, SourceActivity.class));
            return;
        }
        sourceAdapter.submit(sources);
        SourceBean home = ApiConfig.get().getHomeSource();
        if (home != null) sourceAdapter.setCurrentKey(home.getKey());
        tvSourceCount.setText(sources.size() + " 个源");
        etSearch.setText("");
        sourceAdapter.setSearch("");
        sourceAdapter.setFilterChip("全部");
        buildChips();

        if (sourcePopup.isShowing()) {
            sourcePopup.dismiss();
        } else {
            sourcePopup.showAtLocation(find(android.R.id.content),
                    Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    0, 100);
        }
    }

    private void onSourcePicked(SourceBean source) {
        ApiConfig.get().setSourceBean(source);
        sourceAdapter.setCurrentKey(source.getKey());
        final String jar = ApiConfig.get().getSpider();
        if (jar != null && !jar.isEmpty()) {
            OkGo.io(() -> com.yingtu.plus.api.SpiderManager.get().parseJar(jar));
        }
        refreshTitle();
        if (sourcePopup.isShowing()) sourcePopup.dismiss();
        viewPager.setCurrentItem(0, false);
        notifyHomeReload();
    }

    private void notifyHomeReload() {
        viewPager.post(() -> {
            androidx.fragment.app.Fragment f = getSupportFragmentManager().findFragmentByTag("f0");
            if (f instanceof com.yingtu.plus.ui.fragment.HomeMainFragment) {
                ((com.yingtu.plus.ui.fragment.HomeMainFragment) f).reloadForSourceChange();
            }
        });
    }

    private void refreshTitle() {
        SourceBean home = ApiConfig.get().getHomeSource();
        tvTitle.setText(home != null ? "影图 - " + home.getName() : getString(R.string.home_title));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null || viewPager == null) return;
        int tab = intent.getIntExtra(KEY_TAB, -1);
        if (tab >= 0 && tab < tabTitles.length) viewPager.setCurrentItem(tab, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTitle();
    }

    @Override
    public void onBackPressed() {
        if (sourcePopup != null && sourcePopup.isShowing()) {
            sourcePopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }


    // ================= WebHome 适配方法（供 VodFragment / HomeWebController 调用） =================
    // 按影图外壳适配，与上游行为有差异：上游 HomeActivity 是 BottomNavigation + FragmentStateManager
    //（0 点播 / 1 设置 / 2 播放器设置 / 3 增强设置 / 4 弹幕设置），影图是 ViewPager2 5 Tab 外壳
    //（0 主页 / 1 网页首页 / 2 本地文件 / 3 直链收藏 / 4 分享收藏），两者 Tab 语义完全不同，不要按序号对号入座。

    /**
     * 按影图外壳适配，与上游行为有差异：上游 change(position) 在 0 点播 / 1 设置 / 2 播放器设置 /
     * 3 增强设置 / 4 弹幕设置之间切换；影图这里只是把 ViewPager2 切到对应 Tab（0 主页 / 1 网页首页 /
     * 2 本地文件 / 3 直链收藏 / 4 分享收藏）。注意 VodFragment.openSetting() 调的是 change(1)，
     * 在影图会落到「网页首页」Tab 而不是设置页——这是已知差异，留待后续决定是否把 openSetting 改走设置页。
     */
    public void change(int position) {
        if (viewPager == null) return;
        if (position < 0 || position >= tabTitles.length) return;
        viewPager.setCurrentItem(position, false);
    }

    /**
     * 按影图外壳适配，与上游行为有差异：上游 openVod() 复位 WebHomeChromeController、切到底部导航的
     * 点播项并调 VodFragment.openVodHome()；影图没有该控制器与点播导航项，故只做：chrome 回 normal、
     * 恢复 Tab 栏显示、切回 0 号「主页」Tab；若当前已挂载 VodFragment（目前外壳的 5 个 Tab 里没有，
     * 仅作兼容），再调它的 openVodHome()。
     */
    public void openVod() {
        webHomeChromeMode = WebHomeChrome.NORMAL;
        setWebHomeLegacyToolbar(true);
        if (viewPager != null) viewPager.setCurrentItem(0, false);
        VodFragment fragment = findVodFragment();
        if (fragment != null) fragment.openVodHome();
    }

    /**
     * 按影图外壳适配，与上游行为有差异：上游 openEnhanceFromVod() 切到 3 号「增强设置」Fragment；
     * 影图外壳没有增强设置 Tab，唯一的设置入口是 SettingActivity，故打开设置页。
     */
    public void openEnhanceFromVod() {
        startActivity(new Intent(this, SettingActivity.class));
    }

    /**
     * 按影图外壳适配，与上游行为有差异：上游 setWebHomeLegacyToolbar(visible) 控制 Activity 级
     * WebHomeChromeController 的 legacy toolbar 显隐；影图没有该 toolbar，取而代之的是外壳顶部的
     * TabLayout（切 Tab 用），故这里显隐 TabLayout；同时按上游的守卫：未开 WebHome 全屏时强制回 normal。
     */
    public void setWebHomeLegacyToolbar(boolean visible) {
        if (!Setting.isWebHomeFullscreen()) {
            webHomeChromeMode = WebHomeChrome.NORMAL;
            if (tabLayout != null) tabLayout.setVisibility(View.VISIBLE);
            return;
        }
        if (tabLayout != null) tabLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 按影图外壳适配，与上游行为有差异：上游 applyWebHomeDefaultChrome(site) 经 WebHomeChromeController
     * 按站点默认 chrome 布全屏/视口；影图没有该控制器，故只记下 resolve 出的 mode 并转发给已挂载的 VodFragment。
     */
    public void applyWebHomeDefaultChrome(Site site) {
        if (!Setting.isWebHomeFullscreen()) {
            applyChromeToVodFragment(WebHomeChrome.NORMAL);
            return;
        }
        JsonObject chrome = WebHomeChromeStartup.resolve(VodConfig.get().getConfig(), site);
        applyChromeToVodFragment(WebHomeChrome.normalize(chrome == null ? null : (chrome.has("mode") ? chrome.get("mode").getAsString() : null), WebHomeChrome.NORMAL));
    }

    /**
     * 按影图外壳适配，与上游行为有差异：上游 setWebHomeChrome(payload) 经 WebHomeChromeController
     * 应用整包 chrome（含 viewport/沉浸等）；影图没有该控制器，故只做上游的持久化部分（startup 包记下）
     * 与 mode 记录/转发，viewport 保持 EMPTY。
     */
    public void setWebHomeChrome(JsonObject payload) {
        if (!Setting.isWebHomeFullscreen()) {
            applyChromeToVodFragment(WebHomeChrome.NORMAL);
            return;
        }
        if (isStartupChrome(payload)) WebHomeChromeStartup.remember(VodConfig.get().getConfig(), VodConfig.get().getHome(), payload);
        String mode = payload != null && payload.has("mode") ? payload.get("mode").getAsString() : null;
        applyChromeToVodFragment(WebHomeChrome.normalize(mode, WebHomeChrome.NORMAL));
    }

    /**
     * 按影图外壳适配，与上游行为有差异：上游 restoreWebHomeChrome() 经 WebHomeChromeController 恢复
     * 启动时记住的 chrome；影图没有该控制器，故只从 WebHomeChromeStartup.restore() 取回 mode 并转发。
     */
    public void restoreWebHomeChrome() {
        if (!Setting.isWebHomeFullscreen()) {
            applyChromeToVodFragment(WebHomeChrome.NORMAL);
            return;
        }
        JsonObject restored = WebHomeChromeStartup.restore(VodConfig.get().getConfig());
        String mode = restored != null && restored.has("mode") ? restored.get("mode").getAsString() : null;
        applyChromeToVodFragment(WebHomeChrome.normalize(mode, WebHomeChrome.NORMAL));
    }

    /** 当前 WebHome chrome mode（本地保存；无控制器，故初始即 NORMAL）。 */
    public String getWebHomeChromeMode() {
        return webHomeChromeMode;
    }

    /**
     * 按影图外壳适配，与上游行为有差异：上游返回 WebHomeChromeController 实时算出的 viewport；
     * 影图没有该控制器与视口来源，故恒返回本地保存值，初始为 WebHomeViewport.EMPTY。
     */
    public WebHomeViewport getWebHomeViewport() {
        return webHomeViewport == null ? WebHomeViewport.EMPTY : webHomeViewport;
    }

    private boolean isStartupChrome(JsonObject payload) {
        try {
            return payload != null && payload.has("startup") && payload.get("startup").getAsBoolean();
        } catch (Throwable e) {
            return false;
        }
    }

    private void applyChromeToVodFragment(String mode) {
        webHomeChromeMode = WebHomeChrome.normalize(mode, WebHomeChrome.NORMAL);
        VodFragment fragment = findVodFragment();
        if (fragment != null) fragment.applyWebHomeChrome(webHomeChromeMode);
    }

    private VodFragment findVodFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof VodFragment) return (VodFragment) fragment;
            if (fragment != null && fragment.getParentFragment() instanceof VodFragment) return (VodFragment) fragment.getParentFragment();
            if (fragment != null) {
                for (Fragment child : fragment.getChildFragmentManager().getFragments()) {
                    if (child instanceof VodFragment) return (VodFragment) child;
                }
            }
        }
        return null;
    }

    private void applyCustomWallpaper() {
        android.net.Uri uri = com.yingtu.plus.util.WallpaperHelper.getWallpaperUri(this);
        if (uri != null) {
            try {
                android.graphics.Bitmap bmp =
                        android.graphics.BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(uri));
                if (bmp != null) {
                    getWindow().setBackgroundDrawable(
                            new android.graphics.drawable.BitmapDrawable(bmp));
                }
            } catch (Exception ignored) {}
        }
    }

    private void startClock() {
        Runnable tick = new Runnable() {
            final SimpleDateFormat fmt = new SimpleDateFormat("MM-dd EEE", Locale.CHINA);
            @Override
            public void run() {
                tvDate.setText(fmt.format(new Date()));
                clock.postDelayed(this, 1000 * 30);
            }
        };
        clock.post(tick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clock.removeCallbacksAndMessages(null);
        if (sourcePopup != null && sourcePopup.isShowing()) sourcePopup.dismiss();
    }
}
