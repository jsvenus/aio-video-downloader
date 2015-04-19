package activity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.softcsoftware.aio.R;

/**
 * This activity holds the legal information.
 *
 * @author shibaprasad
 * @version 1.0
 */
@SuppressWarnings("FieldCanBeLocal")
public class ALegal extends ABase {

    @SuppressWarnings("FieldCanBeLocal")
    private final String copyrightHoldingNotice = "AIO Downloader App is licenced under " +
            "<b><a href=\"http://opensource.org/licenses/MIT\">The MIT License (MIT)</a></b><br>" +
            "<b>Copyright (c) 2015 - SoftC Software Ptv.</b><br>" +
            "<br>" +
            "Permission is hereby granted, free of charge, to any person obtaining a copy of " +
            "this software and associated documentation files (the \"Software\"), to deal in the " +
            "Software without restriction, including without limitation the rights to use, copy, " +
            "modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, " +
            "and to permit persons to whom the Software is furnished to do so, subject to the following conditions:<br> " +
            "<br>" +
            "The above copyright notice and this permission notice shall be included in all copies " +
            "or substantial portions of the Software.<br>" +
            "<br>" +
            "<i>THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED," +
            "INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR " +
            "PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE " +
            "FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT " +
            "OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR " +
            "OTHER DEALINGS IN THE SOFTWARE.</i>" +
            "<br>";
    private TextView title, copyrightHolder;
    private ImageButton backButton;
    private String androidAssetStudio = "AIO is built with some graphical assets that were created by " +
            "the <b>Android Asset Studio</b>." +
            "<br>" +
            "<a href=\"http://romannurik.github.io/AndroidAssetStudio/\">Android Asset Studio</a> " +
            "is licensed under <a href=\"http://creativecommons.org/licenses/by/3.0/\">CC BY 3.0</a>" +
            "<br>";
    private String universal_image_load =
            "<h><a href=\"https://github.com/nostra13/Android-Universal-Image-Loader\">Universal Image Loader.</a></h>"
                    + "<br>" +
                    "Copyright 2011-2015 Sergey Tarasevich<br>" +
                    "<br>" +
                    "Licensed under the Apache License, Version 2.0 (the \"License\");<br>" +
                    "you may not use this file except in compliance with the License.<br>" +
                    "You may obtain a copy of the License at<br>" +
                    "<br>" +
                    "   http://www.apache.org/licenses/LICENSE-2.0<br>" +
                    "<br>" +
                    "Unless required by applicable law or agreed to in writing, software<br>" +
                    "distributed under the License is distributed on an \"AS IS\" BASIS,<br>" +
                    "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.<br>" +
                    "See the License for the specific language governing permissions and<br>" +
                    "limitations under the License.";

    /**
     * System call back this method when the activity first open.
     *
     * @param bundle system gives the bundle to save the primitive data throughout the life cycle.
     */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_legal);

        initViews();
        initCopyrightHolder();
        initBackOnClick();
    }

    private void initViews() {
        this.title = (TextView) findViewById(R.id.title);
        this.copyrightHolder = (TextView) findViewById(R.id.copyright_holder);
        this.backButton = (ImageButton) findViewById(R.id.back_button);
    }

    private void initBackOnClick() {
        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });
        this.backButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                makeToast(true, "Back to home.");
                return true;
            }
        });
    }

    private void initCopyrightHolder() {
        this.copyrightHolder.setText(Html.fromHtml(copyrightHoldingNotice));
        copyrightHolder.setMovementMethod(LinkMovementMethod.getInstance());

        ((TextView) findViewById(R.id.android_asset_studio)).setText(Html.fromHtml(androidAssetStudio));
        ((TextView) findViewById(R.id.android_asset_studio)).setMovementMethod(LinkMovementMethod.getInstance());

        ((TextView) findViewById(R.id.android_image_loader)).setText(Html.fromHtml(universal_image_load));
        ((TextView) findViewById(R.id.android_image_loader)).setMovementMethod(LinkMovementMethod.getInstance());

    }
}
