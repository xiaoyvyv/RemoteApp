package com.xiaoyv.ssh.terminal.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.drakeet.multitype.MultiTypeAdapter;
import com.romide.terminal.emulatorview.ColorScheme;
import com.romide.terminal.emulatorview.EmulatorView;
import com.romide.terminal.emulatorview.TermSession;
import com.romide.terminal.emulatorview.compat.KeycodeConstants;
import com.trilead.ssh2.Session;
import com.xiaoyv.busines.base.BaseMvpActivity;
import com.xiaoyv.busines.bean.ssh.KeyCodeBean;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.R;
import com.xiaoyv.ssh.databinding.SshActivityTerminalBinding;
import com.xiaoyv.ssh.main.view.EmulatorViewGestureListener;
import com.xiaoyv.ssh.terminal.adapter.TerminalBinder;
import com.xiaoyv.ssh.terminal.contract.TerminalContract;
import com.xiaoyv.ssh.terminal.presenter.TerminalPresenter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * TerminalActivity
 *
 * @author why
 * @since 2020/12/06
 **/
public class TerminalActivity extends BaseMvpActivity<TerminalContract.View, TerminalPresenter> implements TerminalContract.View {
    private static final String KEY_TERMINAL_ENTITY = "KEY_TERMINAL_ENTITY";
    private SshActivityTerminalBinding binding;
    private Session session;
    private SshEntity sshEntity;
    private TerminalBinder terminalBinder;
    private MultiTypeAdapter typeAdapter;
    private ColorScheme colorScheme;
    private EmulatorView emulatorView;


    public static void openSelf(SshEntity sshEntity) {
        Intent starter = new Intent(Utils.getApp(), TerminalActivity.class);
        starter.putExtra(KEY_TERMINAL_ENTITY, sshEntity);
        ActivityUtils.startActivity(starter);
    }

    @Override
    protected TerminalPresenter createPresenter() {
        return new TerminalPresenter();
    }

    @Override
    protected void initIntentData(Intent intent, Bundle bundle) {
        sshEntity = (SshEntity) intent.getSerializableExtra(KEY_TERMINAL_ENTITY);
        if (ObjectUtils.isEmpty(sshEntity)) {
            onBackPressed();
        }
    }

    @Override
    protected View createContentView() {
        binding = SshActivityTerminalBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.toolbar.setStartClickListener(v -> onBackPressed())
                .setTitle(String.format(Locale.getDefault(), "%s：%s", sshEntity.account, sshEntity.ip))
                .setNeedStatusBar(false);
    }

    @Override
    protected void initData() {
        colorScheme = new ColorScheme(ColorUtils.getColor(R.color.ui_text_c4),
                ColorUtils.getColor(R.color.ui_system_black),
                ColorUtils.getColor(R.color.ui_system_black),
                ColorUtils.getColor(R.color.ui_text_c4));

        binding.evTerminal.setBackgroundColor(colorScheme.getBackColor());
        binding.vHr.setBackgroundColor(colorScheme.getForeColor());

        terminalBinder = new TerminalBinder();

        typeAdapter = new MultiTypeAdapter();
        typeAdapter.register(KeyCodeBean.class, terminalBinder);

        binding.rvKeyboard.setAdapter(typeAdapter);

        typeAdapter.setItems(presenter.v2pGetSymbol());
        typeAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onPresenterCreated() {
        presenter.v2pConnectSsh(sshEntity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (emulatorView != null) {
            emulatorView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (emulatorView != null) {
            emulatorView.onPause();
        }
    }

    @Override
    public void p2vConnectSuccess(Session session) {
        InputStream stdout = session.getStdout();
        OutputStream stdin = session.getStdin();

        TermSession termSession = new TermSession(){
            @Override
            protected void onProcessExit() {
                super.onProcessExit();
                finish();
            }
        };
        termSession.setTermIn(stdout);
        termSession.setTermOut(stdin);
        termSession.setColorScheme(colorScheme);
        termSession.setDefaultUTF8Mode(true);
        termSession.setTitle(String.format(Locale.getDefault(), "%s：%s", sshEntity.account, sshEntity.ip));
        termSession.setUpdateCallback(() -> {

        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getDisplay().getRealMetrics(displayMetrics);
        emulatorView = new EmulatorView(this, termSession, displayMetrics);
        binding.evTerminal.addView(emulatorView);

        terminalBinder.bindTerminal(emulatorView);
        emulatorView.setTextSize(12);
        emulatorView.setColorScheme(colorScheme);
        emulatorView.updateSize(false);
        emulatorView.setUseCookedIME(true);
        emulatorView.setBackKeyCharacter(0);
        emulatorView.setAltSendsEsc(false);
        emulatorView.setControlKeyCode(KeycodeConstants.KEYCODE_CTRL_LEFT);
        emulatorView.setFnKeyCode(KeycodeConstants.KEYCODE_FUNCTION);
        emulatorView.setMouseTracking(true);
        emulatorView.setTermType("vt100");
        emulatorView.setCursorBlink(0);
        emulatorView.setCursorBlinkPeriod(250);
        emulatorView.setExtGestureListener(new EmulatorViewGestureListener(emulatorView));
        emulatorView.onResume();

        registerForContextMenu(binding.evTerminal);
    }

    @Override
    protected void onDestroy() {
        ThreadUtils.getCachedPool().execute(() -> {
            if (emulatorView != null) {
                emulatorView.getTermSession().finish();
            }
        });
        super.onDestroy();
    }
}
