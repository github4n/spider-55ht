package com.test.amazon;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/*
 * This class shows how to make a simple authenticated call to the
 * Amazon Product Advertising API.
 *
 * See the README.html that came with this sample for instructions on
 * configuring and running the sample.
 */
public class JavaCodeSnippet {

    /*
     * Your AWS Access Key ID, as taken from the AWS Your Account page.
     */
    private static final String AWS_ACCESS_KEY_ID = "AKIAI77X7X5JVEZ52ZCA";

    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    private static final String AWS_SECRET_KEY = "OErh4pPhz/T+6oOsfvym9bkYOzZ5N7Mh7oKEfWln";

    /*
     * Use the end-point according to the region you are interested in.
     */
    private static final String ENDPOINT = "webservices.amazon.com";

    public static void main(String[] args) {

        /*
         * Set up the signed requests helper.
         */
        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String requestUrl = null;

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemLookup");
        params.put("AWSAccessKeyId", "AKIAI77X7X5JVEZ52ZCA");
        params.put("AssociateTag", "55haitao");
        params.put("ItemId", "B01CPZ3NOW");
        params.put("IdType", "ASIN");
        params.put("ResponseGroup", "Accessories,AlternateVersions,BrowseNodes,EditorialReview,Images,ItemAttributes,ItemIds,Large,Medium,OfferFull,OfferListings,Offers,OfferSummary,PromotionSummary,Reviews,SalesRank,Similarities,Small,Tracks,Variations,VariationImages,VariationMatrix,VariationOffers,VariationSummary");

        requestUrl = helper.sign(params);

        System.out.println("Signed URL: " + requestUrl );
    }
}
