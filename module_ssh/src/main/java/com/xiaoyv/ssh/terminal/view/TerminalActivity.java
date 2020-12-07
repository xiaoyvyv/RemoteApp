package com.xiaoyv.ssh.terminal.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.Utils;
import com.trilead.ssh2.Session;
import com.xiaoyv.busines.base.BaseMvpActivity;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.databinding.SshActivityTerminalBinding;
import com.xiaoyv.ssh.main.view.EmulatorViewGestureListener;
import com.xiaoyv.ssh.terminal.contract.TerminalContract;
import com.xiaoyv.ssh.terminal.presenter.TerminalPresenter;

import java.io.InputStream;
import java.io.OutputStream;

import jackpal.androidterm.emulatorview.ColorScheme;
import jackpal.androidterm.emulatorview.TermSession;

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
                .setTitle(sshEntity.account + "：" + sshEntity.ip)
                .setNeedStatusBar(false);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPresenterCreated() {
        presenter.v2pConnectSsh(sshEntity);
    }

    @Override
    public void p2vConnectSuccess(Session session) {
        InputStream stdout = session.getStdout();
        OutputStream stdin = session.getStdin();

        TermSession termSession = new TermSession();
        termSession.setTermIn(stdout);
        termSession.setTermOut(stdin);
        termSession.setColorScheme(new ColorScheme(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK));
        termSession.setDefaultUTF8Mode(true);
        termSession.setTitle("");
        termSession.initializeEmulator(160, 24);
        termSession.setUpdateCallback(() -> {

        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getDisplay().getRealMetrics(displayMetrics);
        binding.evTerminal.attachSession(termSession);
        binding.evTerminal.setDensity(displayMetrics);
        binding.evTerminal.setTextSize(16);
        binding.evTerminal.setUseCookedIME(true);
        binding.evTerminal.setBackKeyCharacter(32);
        binding.evTerminal.setAltSendsEsc(false);
        binding.evTerminal.setControlKeyCode(0);
        binding.evTerminal.setFnKeyCode(1);
        binding.evTerminal.setMouseTracking(false);
        binding.evTerminal.setTermType("vt100");
        binding.evTerminal.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        binding.evTerminal.setExtGestureListener(new EmulatorViewGestureListener(binding.evTerminal));
        registerForContextMenu(binding.evTerminal);
    }
}
