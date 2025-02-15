/*
 * Copyright (C) 2018 Duy Tran Le
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.duy.calculator.symja.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.view.Gravity;
import android.view.View;

import com.duy.calculator.R;
import com.duy.calculator.activities.base.BaseEvaluatorActivity;
import com.duy.calculator.evaluator.EvaluateConfig;
import com.duy.calculator.evaluator.MathEvaluator;
import com.duy.calculator.evaluator.exceptions.ExpressionChecker;
import com.duy.calculator.evaluator.thread.Command;
import com.duy.calculator.symja.models.LimitItem;
import com.duy.ncalc.calculator.BasicCalculatorActivity;
import com.duy.ncalc.utils.DLog;
import com.gx.common.collect.Lists;

import java.util.ArrayList;

/**
 * Created by Duy on 07-Dec-16.
 */

public class LimitActivity extends BaseEvaluatorActivity {
    private static final String STARTED = LimitActivity.class.getName() + "started";
    private boolean isDataNull = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.limit));
        mHint1.setHint(getString(R.string.enter_function));
        mBtnEvaluate.setText(R.string.eval);

        mLayoutLimit.setVisibility(View.VISIBLE);
        mHint1.setHint("");
        mEditLowerBound.post(new Runnable() {
            @Override
            public void run() {
                mEditLowerBound.setText("x → ");
                mEditLowerBound.setEnabled(false);
                mEditLowerBound.setHint(null);
                mEditLowerBound.setGravity(Gravity.END);
            }
        });

        getIntentData();
        boolean isStarted = mPreferences.getBoolean(STARTED, false);
        if ((!isStarted || DLog.UI_TESTING_MODE) && isDataNull) {
            mInputFormula.setText("1/x + 2");
            mEditUpperBound.setText("inf");
        }

    }

    /**
     * get data from activity start it
     */
    private void getIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(BasicCalculatorActivity.DATA);
        if (bundle != null) {
            String data = bundle.getString(BasicCalculatorActivity.DATA);
            if (data != null) {
                mInputFormula.setText(data);
                isDataNull = false;
                clickEvaluate();
            }
        }
    }

    @Override
    public void clickHelp() {
    }

    @Override
    protected String getExpression() {
        String expr = mInputFormula.getCleanText();
        String limit = mEditUpperBound.getText().toString();

        try {
            ExpressionChecker.checkExpression(limit);
        } catch (Exception e) {
            hideKeyboard();
            handleExceptions(mEditUpperBound, e);
            return null;
        }

        LimitItem limitItem = new LimitItem(expr, limit);
        return limitItem.getInput();
    }

    @Override
    public Command<ArrayList<String>, String> getCommand() {
        return new Command<ArrayList<String>, String>() {
            @WorkerThread
            @Override
            public ArrayList<String> execute(String input) {

                EvaluateConfig config = EvaluateConfig.loadFromSetting(getApplicationContext());
                String fraction = MathEvaluator.getInstance().evaluateWithResultAsTex(input,
                        config.setEvalMode(EvaluateConfig.FRACTION));

                return Lists.newArrayList(fraction);
            }
        };
    }
}
