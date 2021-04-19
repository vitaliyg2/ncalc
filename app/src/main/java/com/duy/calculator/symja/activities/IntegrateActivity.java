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
import android.view.View;

import com.duy.calculator.R;
import com.duy.calculator.activities.base.BaseEvaluatorActivity;
import com.duy.calculator.evaluator.EvaluateConfig;
import com.duy.calculator.evaluator.MathEvaluator;
import com.duy.calculator.evaluator.exceptions.ExpressionChecker;
import com.duy.calculator.evaluator.thread.Command;
import com.duy.calculator.symja.models.IntegrateItem;
import com.duy.ncalc.calculator.BasicCalculatorActivity;
import com.duy.ncalc.utils.DLog;

import java.util.ArrayList;

/**
 * Integrate(f(x), {x, a, b})
 * Integrate of function f(x) with variable x, with lower limit a, upper limit b
 * Created by Duy on 07-Dec-16.
 */
public class IntegrateActivity extends BaseEvaluatorActivity {
    private static final String STARTED = IntegrateActivity.class.getName() + "started";
    private boolean isDataNull = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.integrate));
        mHint1.setHint(getString(R.string.enter_function));
        mBtnEvaluate.setText(R.string.integrate);

        //hide and show view
        mLayoutLimit.setVisibility(View.VISIBLE);
        getIntentData();

        boolean isStarted = mPreferences.getBoolean(STARTED, false);
        if ((!isStarted || DLog.UI_TESTING_MODE) && isDataNull) {
            mInputFormula.setText("sqrt(1-x^2)/x^2");
            mEditLowerBound.setText("sqrt(2)/2");
            mEditUpperBound.setText("1");
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
        String inp = mInputFormula.getCleanText();

        //check empty input
        String from = mEditLowerBound.getText().toString();
        if (from.isEmpty()) {
            mEditLowerBound.requestFocus();
            mEditLowerBound.setError(getString(R.string.enter_limit));
            return null;
        }

        try {
            ExpressionChecker.checkExpression(from);
        } catch (Exception e) {
            hideKeyboard();
            handleExceptions(mEditLowerBound, e);
            return null;
        }

        //check empty input
        String to = mEditUpperBound.getText().toString();
        if (to.isEmpty()) {
            mEditUpperBound.requestFocus();
            mEditUpperBound.setError(getString(R.string.enter_limit));
            return null;
        }

        try {
            ExpressionChecker.checkExpression(to);
        } catch (Exception e) {
            hideKeyboard();
            handleExceptions(mEditUpperBound, e);
            return null;
        }

        IntegrateItem integrateItem = new IntegrateItem(inp,
                mEditLowerBound.getText().toString(),
                mEditUpperBound.getText().toString());
        return integrateItem.getInput();
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

                String decimal = MathEvaluator.getInstance().evaluateWithResultAsTex(input,
                        config.setEvalMode(EvaluateConfig.DECIMAL));

                ArrayList<String> result = new ArrayList<>();
                result.add(fraction);
                result.add(decimal);
                return result;
            }
        };
    }


}
