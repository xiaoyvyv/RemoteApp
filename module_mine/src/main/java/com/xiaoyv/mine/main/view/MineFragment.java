package com.xiaoyv.mine.main.view;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.drakeet.multitype.MultiTypeAdapter;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.xiaoyv.busines.base.BaseFragment;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.FtpEntity;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.busines.utils.ExcelUtils;
import com.xiaoyv.busines.utils.SelectUtils;
import com.xiaoyv.busines.utils.ShareUtils;
import com.xiaoyv.mine.R;
import com.xiaoyv.mine.databinding.MineFragmentDialogBinding;
import com.xiaoyv.mine.databinding.MineFragmentDialogHelpBinding;
import com.xiaoyv.mine.databinding.MineFragmentMainBinding;
import com.xiaoyv.mine.main.adapter.MineItemBinder;
import com.xiaoyv.mine.main.adapter.MineItemHelper;
import com.xiaoyv.ui.listener.SimpleResultListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * FtpFragment
 *
 * @author why
 * @since 2020/11/29
 **/
@Route(path = NavigationPath.PATH_MINE_FRAGMENT)
public class MineFragment extends BaseFragment implements SimpleResultListener<File> {
    private static final String ASSETS_IMPORT_TEMP_PATH = "file/temp.xls";
    private static final int REQUEST_CODE = 5200;
    private static final int IMPORT_TYPE_RDP = 0;
    private static final int IMPORT_TYPE_SSH = 1;
    private static final int IMPORT_TYPE_FTP = 2;
    private MineFragmentDialogBinding dialogBinding;
    private MineFragmentDialogHelpBinding helpBinding;
    private MineFragmentMainBinding binding;
    private DialogPlus bottomSheet;
    private DialogPlus helpDialog;
    private MultiTypeAdapter multiTypeAdapter;
    private int currentImportType = IMPORT_TYPE_RDP;
    /**
     * 导入结果
     */
    private List<List<String>> importResult = new ArrayList<>();

    @Override
    protected View createContentView() {
        binding = MineFragmentMainBinding.inflate(getLayoutInflater());
        dialogBinding = MineFragmentDialogBinding.inflate(getLayoutInflater());
        helpBinding = MineFragmentDialogHelpBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.toolbar.setTitle(StringUtils.getString(R.string.mine_main_title))
                .setEndIcon(R.drawable.ui_icon_setting)
                .setEndClickListener(v -> {

                });
    }

    @Override
    protected void initData() {
        bottomSheet = DialogPlus.newDialog(activity)
                .setContentHolder(new ViewHolder(dialogBinding.getRoot()))
                .setContentBackgroundResource(android.R.color.transparent)
                .setGravity(Gravity.BOTTOM)
                .create();

        helpDialog = DialogPlus.newDialog(activity)
                .setContentHolder(new ViewHolder(helpBinding.getRoot()))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.ui_system_translate)
                .setContentWidth(ScreenUtils.getScreenWidth() - 100)
                .create();

        MineItemBinder itemBinder = new MineItemBinder(activity);
        itemBinder.bindRecycler(dialogBinding.rvItem);
        multiTypeAdapter = new MultiTypeAdapter();
        multiTypeAdapter.register(List.class, itemBinder);
        dialogBinding.rvItem.setAdapter(multiTypeAdapter);

        // 长按交互
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new MineItemHelper(itemBinder));
        itemTouchHelper.attachToRecyclerView(dialogBinding.rvItem);

    }

    @Override
    protected void initListener() {
        // 导入帮助
        binding.tvHelp.setOnClickListener(v -> helpDialog.show());
        helpBinding.tvTemp.setOnClickListener(v -> {
            helpDialog.dismiss();
            ShareUtils.shareAssets(ASSETS_IMPORT_TEMP_PATH);
        });
        helpBinding.tvDone.setOnClickListener(v -> helpDialog.dismiss());

        // 批量导入事件
        binding.clImportRdp.setOnClickListener(v -> {
            currentImportType = IMPORT_TYPE_RDP;
            openSelector();
        });
        binding.clImportSsh.setOnClickListener(v -> {
            currentImportType = IMPORT_TYPE_SSH;
            openSelector();
        });
        binding.clImportFtp.setOnClickListener(v -> {
            currentImportType = IMPORT_TYPE_FTP;
            openSelector();
        });

        // 批量导入
        dialogBinding.tvDone.setOnClickListener(v -> {
            p2vShowLoading("正在导入中");
            ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Object>() {
                @Override
                public Object doInBackground() {
                    for (List<String> row : importResult) {
                        String label = row.size() > 0 ? row.get(0) : "";
                        String group = row.size() > 1 ? row.get(1) : "";
                        String host = row.size() > 2 ? row.get(2) : "";
                        String port = row.size() > 3 ? row.get(3) : "";
                        String account = row.size() > 4 ? row.get(4) : "";
                        String password = row.size() > 5 ? row.get(5) : "";
                        if (currentImportType == IMPORT_TYPE_RDP) {
                            RdpEntity entity = new RdpEntity();
                            entity.label = label;
                            entity.group = group;
                            entity.ip = host;
                            entity.port = port;
                            entity.account = account;
                            entity.password = password;
                            DateBaseManger.get().saveRdp(entity);
                        }
                        if (currentImportType == IMPORT_TYPE_SSH) {
                            SshEntity entity = new SshEntity();
                            entity.label = label;
                            entity.group = group;
                            entity.ip = host;
                            entity.port = port;
                            entity.account = account;
                            entity.password = password;
                            DateBaseManger.get().saveSsh(entity);
                        }
                        if (currentImportType == IMPORT_TYPE_FTP) {
                            FtpEntity entity = new FtpEntity();
                            entity.label = label;
                            entity.group = group;
                            entity.ip = host;
                            entity.port = port;
                            entity.account = account;
                            entity.password = password;
                            DateBaseManger.get().saveFtp(entity);
                        }
                    }
                    return null;
                }

                @Override
                public void onSuccess(Object result) {
                    p2vHideLoading();
                    p2vShowToast("导入完成");
                }
            });
        });
    }

    public void openSelector() {
        // 打开系统的文件选择器
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.ms-excel");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        this.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            SelectUtils.copySelectFile(data.getData(), this);
        }
    }

    @Override
    public void onResult(File selectFile) {
        ExcelUtils.readExcelByCol(selectFile, result -> {
            multiTypeAdapter.setItems(result);
            multiTypeAdapter.notifyDataSetChanged();
            bottomSheet.show();
        });
        ExcelUtils.readExcelByRow(selectFile, result -> {
            this.importResult = result;
        });
    }

    @Override
    public boolean onFragmentBackPressed() {
        return true;
    }
}
