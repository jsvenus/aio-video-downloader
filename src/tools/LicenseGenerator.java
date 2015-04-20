package tools;

/**
 * This class is useful for generating a third party license strings.
 *
 * @version 1.0
 *          Created by shibaprasad on 4/14/2015.
 */
public class LicenseGenerator {

    /**
     * Generate a apache license.
     *
     * @param copyrightYear           the year of the copyright license.
     * @param ownerOfCopyrightLicense the owner name of the license.
     * @return The Apache License.
     */
    public static String getApacheLicense(String productName, String copyrightYear, String ownerOfCopyrightLicense) {
        return "The following software may be included in this product : <b>" + productName + "</b>. " +
                "This software contains the following license and notice below :<br/><br/>" +
                "Copyright (c) " + copyrightYear + "  <b>" + ownerOfCopyrightLicense + "</b><br/><br/>" +
                "" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");<br/>" +
                "you may not use this file except in compliance with the License." +
                "You may obtain a copy of the License at" +
                "<br/><br/>" +
                "<a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a>" +
                "<br/><br/>" +
                "Unless required by applicable law or agreed to in writing, software" +
                "distributed under the License is distributed on an \"AS IS\" BASIS," +
                "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                "See the License for the specific language governing permissions and" +
                "limitations under the License.";
    }

    /**
     * Generate a Creative Commons license.
     *
     * @param isHtml                  indicate whether the license will generating in html form or not.
     * @param licenseLink             the license version.
     * @param productName             the product name.
     * @param authorName              the author name.
     * @param linkAddressOfTheProduct the link of the product home.
     * @return the license.
     */
    public static String getCCLicense(boolean isHtml, String licenseLink, String productName, String authorName,
                                      String linkAddressOfTheProduct) {
        String license = "The following software may be included in this product : " + productName + ". " +
                "This software contains the following license and notice below :\n" +
                "" +
                "<a href=\"" + linkAddressOfTheProduct + "\">" + authorName + "</a> " +
                "is licensed under <a href=\"http://creativecommons.org/licenses/by/" + licenseLink + "/\">CC BY 3.0</a>" +
                "\n";

        if (isHtml) {
            return "The following software may be included in this product : " + "<b>" + productName + "</b>. " +
                    "This software contains the following license and notice below :<br/>" +
                    "" +
                    "<a href=\"" + linkAddressOfTheProduct + "\">" + authorName + "</a> " +
                    "is licensed under <a href=\"" + licenseLink + "\">CC BY 3.0</a>" +
                    "<br/>";
        }
        return license;
    }
}
