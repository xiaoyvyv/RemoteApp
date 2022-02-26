package com.xiaoyv.mine.main.view

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import com.chad.library.adapter.base.BaseBinderAdapter
import com.xiaoyv.blueprint.base.BaseFragment
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.FtpEntity
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.busines.utils.ExcelUtils
import com.xiaoyv.busines.utils.SelectUtils
import com.xiaoyv.busines.utils.ShareUtils
import com.xiaoyv.desktop.mine.R
import com.xiaoyv.desktop.mine.databinding.MineFragmentDialogBinding
import com.xiaoyv.desktop.mine.databinding.MineFragmentDialogHelpBinding
import com.xiaoyv.desktop.mine.databinding.MineFragmentMainBinding
import com.xiaoyv.mine.main.adapter.MineItemBindingBinder
import com.xiaoyv.mine.main.adapter.MineItemHelper
import com.xiaoyv.ui.dialog.normal.NormalDialog
import com.xiaoyv.ui.listener.SimpleResultListener
import java.io.File

/**
 * FtpFragment
 *
 * @author why
 * @since 2020/11/29
 */
@Route(path = NavigationPath.PATH_MINE_FRAGMENT)
class MineFragment : BaseFragment(), SimpleResultListener<File> {
    private var dialogBinding: MineFragmentDialogBinding? = null
    private var helpBinding: MineFragmentDialogHelpBinding? = null

    private lateinit var binding: MineFragmentMainBinding
    private lateinit var multiTypeAdapter: BaseBinderAdapter

    private var bottomSheet: NormalDialog? = null
    private var helpDialog: NormalDialog? = null
    private var currentImportType = IMPORT_TYPE_RDP

    /**
     * 导入结果
     */
    private var importResult: List<List<String>> = ArrayList()
    override fun createContentView(): View {
        binding = MineFragmentMainBinding.inflate(layoutInflater)
        dialogBinding = MineFragmentDialogBinding.inflate(layoutInflater)
        helpBinding = MineFragmentDialogHelpBinding.inflate(
            layoutInflater
        )
        return binding.root
    }

    override fun initView() {
        binding.toolbar.title = StringUtils.getString(R.string.mine_main_title)
        binding.toolbar.setRightIcon(R.drawable.ui_icon_setting)
    }

    override fun initData() {
        bottomSheet = NormalDialog(hostActivity)
        bottomSheet!!.customView = dialogBinding!!.root
        helpDialog = NormalDialog(hostActivity)
        helpDialog!!.customView = helpBinding!!.root
        val itemBinder = MineItemBindingBinder()
        multiTypeAdapter = BaseBinderAdapter()
        multiTypeAdapter.addItemBinder(itemBinder)
        dialogBinding!!.rvItem.adapter = multiTypeAdapter

        // 长按交互
        val itemTouchHelper = ItemTouchHelper(MineItemHelper(itemBinder))
        itemTouchHelper.attachToRecyclerView(dialogBinding!!.rvItem)
    }

    override fun initListener() {
        // 导入帮助
        binding.tvHelp.setOnClickListener { v: View? -> helpDialog!!.show() }
        helpBinding!!.tvTemp.setOnClickListener { v: View? ->
            helpDialog!!.dismiss()
            ShareUtils.shareAssets(ASSETS_IMPORT_TEMP_PATH)
        }
        helpBinding!!.tvDone.setOnClickListener { v: View? -> helpDialog!!.dismiss() }

        // 批量导入事件
        binding.clImportRdp.setOnClickListener { v: View? ->
            currentImportType = IMPORT_TYPE_RDP
            openSelector()
        }
        binding.clImportSsh.setOnClickListener { v: View? ->
            currentImportType = IMPORT_TYPE_SSH
            openSelector()
        }
        binding.clImportFtp.setOnClickListener { v: View? ->
            currentImportType = IMPORT_TYPE_FTP
            openSelector()
        }

        // 批量导入
        dialogBinding!!.tvDone.setOnClickListener {
            p2vShowLoading("正在导入中")
            ThreadUtils.executeByCached(object : SimpleTask<Any>() {
                override fun doInBackground(): Any {
                    for (row in importResult) {
                        val label = if (row.isNotEmpty()) row[0] else ""
                        val group = if (row.size > 1) row[1] else ""
                        val host = if (row.size > 2) row[2] else ""
                        val port = if (row.size > 3) row[3] else ""
                        val account = if (row.size > 4) row[4] else ""
                        val password = if (row.size > 5) row[5] else ""
                        if (currentImportType == IMPORT_TYPE_RDP) {
                            val entity = RdpEntity()
                            entity.label = label
                            entity.group = group
                            entity.ip = host
                            entity.port = port
                            entity.account = account
                            entity.password = password
                            DateBaseManger.get().saveRdp(entity)
                        }
                        if (currentImportType == IMPORT_TYPE_SSH) {
                            val entity = SshEntity()
                            entity.label = label
                            entity.group = group
                            entity.ip = host
                            entity.port = port
                            entity.account = account
                            entity.password = password
                            DateBaseManger.get().saveSsh(entity)
                        }
                        if (currentImportType == IMPORT_TYPE_FTP) {
                            val entity = FtpEntity()
                            entity.label = label
                            entity.group = group
                            entity.ip = host
                            entity.port = port
                            entity.account = account
                            entity.password = password
                            DateBaseManger.get().saveFtp(entity)
                        }
                    }
                    return Unit
                }

                override fun onSuccess(result: Any) {
                    p2vHideLoading()
                    p2vShowToast("导入完成")
                }
            })
        }
    }

    fun openSelector() {
        // 打开系统的文件选择器
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/vnd.ms-excel"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        this.startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            SelectUtils.copySelectFile(data.data!!, this)
        }
    }

    override fun onResult(selectFile: File) {
        ExcelUtils.readExcelByCol(selectFile) { result: List<List<String>> ->
            multiTypeAdapter.setList(result)
            multiTypeAdapter.notifyDataSetChanged()
            bottomSheet!!.show()
        }
        ExcelUtils.readExcelByRow(selectFile) { result: List<List<String>> ->
            importResult = result
        }
    }

    override fun onFragmentBackPressed(): Boolean {
        return true
    }

    companion object {
        private const val ASSETS_IMPORT_TEMP_PATH = "file/temp.xls"
        private const val REQUEST_CODE = 5200
        private const val IMPORT_TYPE_RDP = 0
        private const val IMPORT_TYPE_SSH = 1
        private const val IMPORT_TYPE_FTP = 2
    }
}