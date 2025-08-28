package com.github.xesam.android.timer.example.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.xesam.android.timer.AndroidTimer;
import com.github.xesam.android.timer.AndroidTick;
import com.github.xesam.android.timer.CountDownTimer;
import com.github.xesam.android.timer.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AndroidTimer mAndroidTimer;
    private CountDownTimer mCountDownTimer;
    private AndroidTick androidTick;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final TextView countdownTextView = binding.textCountdown;
        homeViewModel.getCountDownText().observe(getViewLifecycleOwner(), countdownTextView::setText);
        mAndroidTimer = new AndroidTimer(1000) {
            @Override
            public void onTick(long millisFly) {
                homeViewModel.setText(mAndroidTimer.getState() + ":" + millisFly);
            }

            @Override
            public void onCancel(long millisFly) {
                homeViewModel.setText(mAndroidTimer.getState() + ":" + millisFly);
            }
        };
        binding.btnStart.setOnClickListener(v -> mAndroidTimer.start());
        binding.btnPause.setOnClickListener(v -> mAndroidTimer.pause());
        binding.btnResume.setOnClickListener(v -> mAndroidTimer.resume());
        binding.btnCancel.setOnClickListener(v -> mAndroidTimer.cancel());

        // 初始化CountDownTimer，设置10秒倒计时，间隔1秒
        mCountDownTimer = new CountDownTimer(5_000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                homeViewModel.setCountDownText(mCountDownTimer.getState() + ":" + millisUntilFinished);
            }

            @Override
            public void onFinish(long duration) {
                homeViewModel.setCountDownText("CountDown Finished!" + duration);
            }

            @Override
            public void onCancel(long millisUntilFinished) {
                homeViewModel.setCountDownText(mCountDownTimer.getState() + ":" + millisUntilFinished);
            }

            @Override
            public void onPause(long millisUntilFinished) {
                homeViewModel.setCountDownText(mCountDownTimer.getState() + ":" + millisUntilFinished);
            }

            @Override
            public void onResume(long millisUntilFinished) {
                homeViewModel.setCountDownText(mCountDownTimer.getState() + ":" + millisUntilFinished);
            }
        };

        binding.btnStartCountdown.setOnClickListener(v -> mCountDownTimer.start());
        binding.btnPauseCountdown.setOnClickListener(v -> mCountDownTimer.pause());
        binding.btnResumeCountdown.setOnClickListener(v -> mCountDownTimer.resume());
        binding.btnCancelCountdown.setOnClickListener(v -> mCountDownTimer.cancel());

        androidTick = new AndroidTick(1000) {
            @Override
            protected void onTick(AndroidTick thisInstance, int count) {
                binding.textDelay.setText("AndroidDelay:" + count);
            }
        };

        binding.btnStartDelay.setOnClickListener(v -> androidTick.tick());
        binding.btnCancelDelay.setOnClickListener(v -> androidTick.cancel());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (mAndroidTimer != null) {
            mAndroidTimer.cancel();
        }
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        if (androidTick != null) {
            androidTick.cancel();
        }
    }
}