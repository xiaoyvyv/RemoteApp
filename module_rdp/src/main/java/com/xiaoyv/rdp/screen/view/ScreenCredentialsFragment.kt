package com.xiaoyv.rdp.screen.view

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ScreenUtils
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpActivityScreenCredentialsBinding
import com.xiaoyv.ui.kotlin.dp
import java.io.Serializable

/**
 * ScreenCredentialsFragment
 *
 * @author why
 * @since 2021/07/06
 **/
class ScreenCredentialsFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return RdpActivityScreenCredentialsBinding.inflate(inflater, container, false).root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = RdpActivityScreenCredentialsBinding.bind(view)
        isCancelable = false

        binding.ivClose.setOnClickListener { dismiss() }

        val builder =
            (arguments?.getSerializable(NavigationKey.KEY_SERIALIZABLE) as? Builder) ?: return
        binding.tvTitle.text = builder.title
        binding.tvSubtitle.text = builder.subtitle
        binding.tvCredentials.text = builder.credentials

        binding.editTextUsername.setText(builder.username)
        binding.editTextPassword.setText(builder.password)
        binding.editTextDomain.setText(builder.domain)

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
            builder.done.invoke(
                binding.editTextUsername.text.toString(),
                binding.editTextPassword.text.toString(),
                binding.editTextDomain.text.toString()
            )
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
                    width = ScreenUtils.getAppScreenWidth() - 60.dp()
                }
            }
        }
    }

    class Builder() : Serializable, Parcelable {
        internal var title: String = "--"
        internal var subtitle: String = "--"
        internal var credentials: String = "--"
        internal var username = ""
        internal var password = ""
        internal var domain = ""
        internal var cancel: () -> Unit = {}
        internal var done: (String, String, String) -> Unit = { _, _, _ -> }

        constructor(parcel: Parcel) : this() {
            title = parcel.readString().toString()
            subtitle = parcel.readString().toString()
            credentials = parcel.readString().toString()
            username = parcel.readString().toString()
            password = parcel.readString().toString()
            domain = parcel.readString().toString()
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: String): Builder {
            this.subtitle = subtitle
            return this
        }

        fun setCredentials(credentials: String): Builder {
            this.credentials = credentials
            return this
        }

        fun setData(username: String, password: String, domain: String): Builder {
            this.username = username
            this.password = password
            this.domain = domain
            return this
        }

        fun setCancel(cancel: () -> Unit = {}): Builder {
            this.cancel = cancel
            return this
        }

        fun setDone(done: (String, String, String) -> Unit = { _, _, _ -> }): Builder {
            this.done = done
            return this
        }

        fun build() = ScreenCredentialsFragment().also {
            it.arguments = Bundle().apply {
                putSerializable(NavigationKey.KEY_SERIALIZABLE, this@Builder)
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(subtitle)
            parcel.writeString(credentials)
            parcel.writeString(username)
            parcel.writeString(password)
            parcel.writeString(domain)
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