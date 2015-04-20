package activity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.softcsoftware.aio.R;
import tools.LicenseGenerator;

/**
 * Legal notice Activity is responsible for showing the legal information
 * about the app.
 * Created by shibaprasad on 3/15/2015.
 */
public class ALegal extends ABase {

    private LinearLayout licenseListLayout;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        //set the activity screen.
        setContentView(R.layout.activity_legal);

        //get the license list layout.
        licenseListLayout = (LinearLayout) findViewById(R.id.licenseList);

        addProjectLicense();
        //android asset studio.
        addAndroidAssetStudio();
        //So license
        addSOLicense();
        addMaterialLicense();
        
    }

    /**
     * Back Pressed Call back method.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    private void addProjectLicense() {
        String projectLicense = "<b>AIO Video Downloader is a open source application.</B><br/>" +
                "-----------------------------------------<br/>" +
                "Copyright (C) <b>2015 Shiba Prasad Jana</b><br/>" +
                "<br/><br/>" +
                "This program is free software; you can redistribute it and/or" +
                "modify it under the terms of the GNU General Public License" +
                "as published by the Free Software Foundation; either version 2" +
                "of the License, or any later version." +
                "<br/><br/>" +
                "This program is distributed in the hope that it will be useful," +
                "but WITHOUT ANY WARRANTY; without even the implied warranty of" +
                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" +
                "GNU General Public License for more details.<br/><br/>" +
                "" +
                "You should have received a copy of the GNU General Public License" +
                "along with this program; if not, write to the Free Software" +
                "Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.";
        addLicense(projectLicense);
    }

    /**
     * Android asset studio license.
     */
    private void addAndroidAssetStudio() {
        String license = LicenseGenerator.getCCLicense(true,
                "http://creativecommons.org/licenses/by/3.0/", "Android Asset Studio",
                "Android Asset Studio", "http://romannurik.github.io/AndroidAssetStudio/");
        addLicense(license);
    }

    /**
     * Android asset studio license.
     */
    private void addSOLicense() {
        String license =
                "Some portions of this product may be from <b>Stack Overflow or the Stack Exchange network's contributed content</B>.\n" +
                        "All the content contributed to Stack Overflow or other Stack Exchange sites is " +
                        "<a href=\"http://creativecommons.org/licenses/by-sa/3.0/\"> cc-wiki (aka cc-by-sa)</a> licensed.";
        addLicense(license);
    }

    /**
     * Material license.
     */
    private void addMaterialLicense() {
        String license = LicenseGenerator.getApacheLicense("MaterialDesignLibrary", "2014", " Ivan Navas.");
        addLicense(license);
    }

    /**
     * Add a new view to list.
     *
     * @param license the license text.
     */
    private void addLicense(String license) {
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
