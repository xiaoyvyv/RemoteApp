package com.xiaoyv.mine.main.view;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.StringUtils;
import com.drakeet.multitype.MultiTypeAdapter;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.xiaoyv.busines.base.BaseFragment;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.utils.ExcelUtils;
import com.xiaoyv.busines.utils.SelectUtils;
import com.xiaoyv.mine.R;
import com.xiaoyv.mine.databinding.MineFragmentDialogBinding;
import com.xiaoyv.mine.databinding.MineFragmentMainBinding;
import com.xiaoyv.mine.main.adapter.MineItemBinder;
import com.xiaoyv.mine.main.adapter.MineItemHelper;
import com.xiaoyv.ui.listener.SimpleResultListener;

import java.io.File;
import java.util.List;

/**
 * FtpFragment
 *
 * @author why
 * @since 2020/11/29
 **/
@Route(path = NavigationPath.PATH_MINE_FRAGMENT)
public class MineFragment extends BaseFragment implements SimpleResultListener<File> {
    private static final int REQUEST_CODE = 5200;
    private MineFragmentDialogBinding dialogBinding;
    private MineFragmentMainBinding binding;
    private DialogPlus bottomSheet;
    private MultiTypeAdapter multiTypeAdapter;

    @Override
    protected View createContentView() {
        binding = MineFragmentMainBinding.inflate(getLayoutInflater());
        dialogBinding = MineFragmentDialogBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.toolbar.setTitle(StringUtils.getString(R.string.mine_main_title))
                .setEndIcon(R.drawable.ui_icon_setting)
                .setEndClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

    }

    @Override
    protected void initData() {
        bottomSheet = DialogPlus.newDialog(activity)
                .setCancelable(false)
                .setContentHolder(new ViewHolder(dialogBinding.getRoot()))
                .setContentBackgroundResource(android.R.color.transparent)
                .setGravity(Gravity.BOTTOM)
                .create();

        MineItemBinder itemBinder = new MineItemBinder();
        multiTypeAdapter = new MultiTypeAdapter();
        multiTypeAdapter.register(List.class, itemBinder);
        dialogBinding.rvItem.setAdapter(multiTypeAdapter);

        // 长按交互
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new MineItemHelper(itemBinder));
        itemTouchHelper.attachToRecyclerView(dialogBinding.rvItem);
    }

    @Override
    protected void initListener() {
        binding.tvHelp.setOnClickListener(v -> {

        });

        binding.clImportRdp.setOnClickListener(v -> {
            // 打开系统的文件选择器
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.ms-excel");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            this.startActivityForResult(intent, REQUEST_CODE);
        });
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
    }
}
