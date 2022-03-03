package com.xiaoyv.busines.ftp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.xiaoyv.desktop.business.R
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpItemBinding
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpPathBinding
import com.xiaoyv.widget.binder.BaseItemBindingBinder

/**
 * BaseFtpBinder
 *
 * @author why
 * @since 2022/2/28
 */
class BaseFtpBinder : BaseItemBindingBinder<BaseFtpFile, BusinessActivityFtpItemBinding>() {

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): BusinessActivityFtpItemBinding {
        return BusinessActivityFtpItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(
        holder: BinderVBHolder<BusinessActivityFtpItemBinding>,
        binding: BusinessActivityFtpItemBinding,
        data: BaseFtpFile
    ) {
        val fileExtension = FileUtils.getFileExtension(data.fileName)
        when {
            data.isDirectory -> {
                binding.ivIcon.setImageResource(R.drawable.business_icon_file_dir)
            }
            data.isSymlink -> {
                binding.ivIcon.setImageResource(R.drawable.business_icon_file_link)
            }
            else -> {
                binding.ivIcon.setImageResource(fileExtension.toTypeIcon())
            }
        }

        // 权限
        val permission = data.permission.let {
            val length = it.length
            if (length >= 3) {
                it.subSequence(length - 3, length)
            } else {
                it
            }
        }

        binding.tvLabel.text = data.fileName
        binding.tvTime.text = TimeUtils.getFriendlyTimeSpanByNow(data.modifierTime)
        binding.tvPermission.text = String.format("%s/%s", permission, data.user)
        binding.tvSize.text = ConvertUtils.byte2FitMemorySize(data.size,2)

        holder.addClickListener(binding.root, data)
    }

    @DrawableRes
    private fun String.toTypeIcon(): Int {
        return when (this) {
            "txt" -> R.drawable.business_icon_file_txt
            "db", "csv", "wdb", "dbf", "mdb", "mdf", "sql" -> R.drawable.business_icon_file_db
            "ini", "conf", "config", "gradle" -> R.drawable.business_icon_file_ini
            "wps", "xls", "xlsx", "ppt", "pptx", "doc", "docx", "pdf" -> R.drawable.business_icon_file_wps
            "xml", "htm", "html", "xhtml" -> R.drawable.business_icon_file_xml
            "zip", "rar", "tar", "arj", "z", "7z", "bz", "bz2", "gz", "xz", "gzip", "bzip2" -> R.drawable.business_icon_file_zip
            else -> R.drawable.business_icon_file_unknown
        }
    }
}

/**
 * 路径 Binder
 */
class BaseFtpPathBinder : BaseItemBindingBinder<String, BusinessActivityFtpPathBinding>() {

    override fun convert(
        holder: BinderVBHolder<BusinessActivityFtpPathBinding>,
        binding: BusinessActivityFtpPathBinding,
        data: String
    ) {
        binding.tvPath.text = data

        holder.addClickListener(binding.tvPath, data)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): BusinessActivityFtpPathBinding {
        return BusinessActivityFtpPathBinding.inflate(layoutInflater, parent, false)
    }

}
