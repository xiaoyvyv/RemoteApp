package com.xiaoyv.rdp.screen.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ScreenUtils
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpActivityScreenCertificateBinding
import com.xiaoyv.ui.kotlin.dp
import java.io.Serializable

/**
 * ScreenCertificateFragment
 *
 * @author why
 * @since 2021/7/5
 */
class ScreenCertificateFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return RdpActivityScreenCertificateBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = RdpActivityScreenCertificateBinding.bind(view)
        isCancelable = false

        binding.ivClose.setOnClickListener { dismiss() }

        val builder =
            (arguments?.getSerializable(NavigationKey.KEY_SERIALIZABLE) as? Builder) ?: return
        binding.tvNameValue.text = builder.certName
        binding.tvFinger.text = builder.finger
        binding.ivClose.setOnClickListener {
            dismiss()
            builder.cancel.invoke()
        }
        binding.btNo.setOnClickListener {
            dismiss()
            builder.cancel.invoke()
        }
        binding.btYes.setOnClickListener {
            dismiss()
            builder.done.invoke()
        }
    }


    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, javaClass.simpleName)
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.window?.let { window ->
                window.setBackgroundDrawableResource(R.color.ui_system_translate)
                window.attributes = window.attributes.apply {
                    dimAmount = 0.2f
                    width = ScreenUtils.getScreenWidth() - 60.dp()
                }
            }
        }
    }

    class Builder : Serializable {
        internal var finger: String = "--"
        internal var certName: String = "--"
        internal var cancel: () -> Unit = {}
        internal var done: () -> Unit = {}

        fun setCertName(certName: String): Builder {
            this.certName = certName
            return this
        }

        fun setFinger(finger: String): Builder {
            this.finger = finger
            return this
        }

        fun setCancel(cancel: () -> Unit = {}): Builder {
            this.cancel = cancel
            return this
        }

        fun setDone(done: () -> Unit = {}): Builder {
            this.done = done
            return this
        }

        fun build() = ScreenCertificateFragment().also {
            it.arguments = Bundle().apply {
                putSerializable(NavigationKey.KEY_SERIALIZABLE, this@Builder)
            }
        }
    }
}