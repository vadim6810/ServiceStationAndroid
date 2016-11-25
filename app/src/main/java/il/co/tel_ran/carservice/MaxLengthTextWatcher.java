package il.co.tel_ran.carservice;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;

/**
 * Created by maxim on 25-Nov-16.
 */

public class MaxLengthTextWatcher implements TextWatcher {

    private final int mMaxMessageLEngth;

    private final TextInputLayout mInputLayout;
    private final EditText mEditText;

    private final String mEmptyErrorMessage;
    private final String mExceedingErrorMessage;
    private final int mExceedingColor;

    public MaxLengthTextWatcher(int maxLength, @Nullable TextInputLayout inputLayout, @NonNull EditText editText,
                                @Nullable String emptyErrorMessage, @NonNull String exceedingErrorMessage,
                                @ColorInt int exceedingColor) {
        mMaxMessageLEngth = maxLength;

        mInputLayout = inputLayout;
        mEditText= editText;

        mEmptyErrorMessage = emptyErrorMessage;
        mExceedingErrorMessage = exceedingErrorMessage;
        mExceedingColor = exceedingColor;
    }

    private int mLastLength;

    private boolean mSpanSet = false;
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        int length = s.length();

        // Make sure we don't get stuck in an infinite loop.
        if (mLastLength == length)
            return;
        mLastLength = length;

        if (length == 0) {
            // User has to type at least something
            if (mInputLayout != null && mEmptyErrorMessage != null) {
                mInputLayout.setError(mEmptyErrorMessage);
            }
        }

        // Notify the user that he has exceeded the allowed character count.
        if (length > mMaxMessageLEngth) {
            // Apply span if we haven't already.
            if (!mSpanSet) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
                        s);
                // Color the exceeding characters to let the user know which characters are exceeding.
                spannableStringBuilder.setSpan(new ForegroundColorSpan(mExceedingColor),
                        mMaxMessageLEngth, length,
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                // Apply the spanned text.
                mEditText.setText(spannableStringBuilder);
                // Move the cursor back to the end of the text.
                mEditText.setSelection(length);
                mSpanSet = true;
            }
            // Show the error message.
            if (mInputLayout != null && mInputLayout.getError() == null) {
                mInputLayout.setError(mExceedingErrorMessage);
            }
        } else {
            // Make sure we don't accidentally remove the previous error message if the length is still at 0.
            if (mInputLayout != null && length > 0)
                mInputLayout.setError(null);

            // Remove spans because the length is no longer exceeding the maximum amount of allowed characters.
            if (mSpanSet) {
                ForegroundColorSpan[] spans = s.getSpans(0, length,
                        ForegroundColorSpan.class);
                if (spans.length > 0)
                    s.removeSpan(spans[0]);
            }
            mSpanSet = false;
        }
    }
}
