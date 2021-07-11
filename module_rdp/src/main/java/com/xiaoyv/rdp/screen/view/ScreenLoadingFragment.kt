package com.xiaoyv.rdp.screen.view

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ScreenUtils
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpActivityScreenLoadingBinding
import com.xiaoyv.ui.kotlin.dp
import java.io.Serializable

/**
 * ScreenLoadingFragment
 *
 * @author why
 * @since 2021/07/06
 **/
class ScreenLoadingFragment : DialogFragment() {
    var binding: RdpActivityScreenLoadingBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return RdpActivityScreenLoadingBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = RdpActivityScreenLoadingBinding.bind(view)
        isCancelable = false

        binding.ivClose.setOnClickListener { dismiss() }

        val builder =
            (arguments?.getSerializable(NavigationKey.KEY_SERIALIZABLE) as? Builder) ?: return
        binding.tvTitle.text = builder.title
        binding.tvLoadingTitle.text = builder.loadingTitle
        binding.tvLoadingMessage.text = builder.loadingMessage

        binding.ivClose.setOnClickListener {
            dismiss()
            builder.cancel.invoke()
        }
        binding.btNo.setOnClickListener {
            dismiss()
            builder.cancel.invoke()
        }

        this.binding = binding
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
                    width = ScreenUtils.getAppScreenWidth() - 60.dp()
                }
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
    }

    class Builder() : Serializable, Parcelable {
        internal var title: String = "--"
        internal var loadingTitle: String = "--"
        internal var loadingMessage: String = "--"

        internal var cancel: () -> Unit = {}

        constructor(parcel: Parcel) : this() {
            title = parcel.readString().toString()
            loadingTitle = parcel.readString().toString()
            loadingMessage = parcel.readString().toString()
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setLoadingTitle(loadingTitle: String): Builder {
            this.loadingTitle = loadingTitle
            return this
        }

        fun setLoadingMessage(loadingMessage: String): Builder {
            this.loadingMessage = loadingMessage
            return this
        }

        fun setCancel(cancel: () -> Unit = {}): Builder {
            this.cancel = cancel
            return this
        }

        fun build() = ScreenLoadingFragment().also {
            it.arguments = Bundle().apply {
                putSerializable(NavigationKey.KEY_SERIALIZABLE, this@Builder)
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(loadingTitle)
            parcel.writeString(loadingMessage)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Builder> {
            override fun createFromParcel(parcel: Parcel): Builder {
                return Builder(parcel)
            }

            override fun newArray(size: Int): Array<Builder?> {
                return arrayOfNulls(size)
            }
        }
    }
}