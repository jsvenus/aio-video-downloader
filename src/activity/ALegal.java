package activity;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.softcsoftware.aio.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import tools.LicenseGenerator;

/**
 * Legal notice Activity is responsible for showing the legal information
 * about the app.
 * Created by shibaprasad on 3/15/2015.
 */
@EActivity(R.layout.activity_legal)
public class ALegal extends ABase {

    @ViewById(R.id.licenseList)
    LinearLayout licenseListLayout;

    @AfterViews
    void updateUI() {
        add_product_license();
        add_android_asset_studio();
        add_so_credit();
        add_material_design_lib();
        add_android_annotation_license();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    void add_product_license() {
        String projectLicense = "<b>AIO Video Downloader is a open source application.</B><br/>" +
                "-----------------------------------------<br/>" +
                "The MIT License (MIT)" +
                "<br/>" +
                "Copyright (c) <b>2015 Shiba Prasad J.</b>" +
                "<br/><br/>" +
                "Permission is hereby granted, free of charge, to any person obtaining a copy " +
                "of this software and associated documentation files (the \"Software\"), to deal " +
                "in the Software without restriction, including without limitation the rights " +
                "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell " +
                "copies of the Software, and to permit persons to whom the Software is " +
                "furnished to do so, subject to the following conditions: " +
                "<br/><br/>" +
                "The above copyright notice and this permission notice shall be included in all " +
                "copies or substantial portions of the Software. " +
                "<br/><br/>" +
                "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR " +
                "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, " +
                "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE " +
                "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER " +
                "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, " +
                "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE " +
                "SOFTWARE. ";
        addLicense(projectLicense);
    }

    void add_android_asset_studio() {
        String license = "Some of the graphical assets of the product were created by " +
                "the <b>Android Asset Studio</b>." +
                "<br>" +
                "<a href=\"http://romannurik.github.io/AndroidAssetStudio/\">Android Asset Studio</a> " +
                "is licensed under <a href=\"http://creativecommons.org/licenses/by/3.0/\">CC BY 3.0</a>";
        addLicense(license);
    }

    void add_so_credit() {
        String license =
                "Some portions of this product may be from <b>Stack Overflow or the Stack Exchange network's contributed content</B>.\n" +
                        "All the content contributed to Stack Overflow or other Stack Exchange sites is " +
                        "<a href=\"http://creativecommons.org/licenses/by-sa/3.0/\"> cc-wiki (aka cc-by-sa)</a> licensed.";
        addLicense(license);
    }

    void add_material_design_lib() {
        String license = LicenseGenerator.getApacheLicense("MaterialDesignLibrary", "2014", " Ivan Navas.");
        addLicense(license);
    }

    void add_android_annotation_license() {
        String license = LicenseGenerator.getApacheLicense("AndroidAnnotations", "2012-2015", "eBusiness Information");
        addLicense(license);
    }

    /**
     * Add a new view to list.
     *
     * @param license the license text.
     */
    void addLicense(String license) {
        View view = View.inflate(this, R.layout.layout_license_list_row, null);
        TextView textView = (TextView) view;
        textView.setText(Html.fromHtml(license));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        licenseListLayout.addView(textView);
    }

    public void onBack(View view) {
        finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}
