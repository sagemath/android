package org.sagemath.singlecellserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/*
 * 
 * jQuery({
 *   "content": [{
 *     "parent_header": {
 *       "username": "", "msg_id": "749529bd-bcfe-43a7-b660-2cea20df3f32", 
 *       "session": "7af9a99a-2a0d-438f-8576-5e0bd853501a"}, 
 *     "msg_type": "extension", 
 *     "sequence": 0, 
 *     "output_block": null, 
 *     "content": {
 *       "content": {
 *         "interact_id": "8157151862156143292", 
 *         "layout": {"top_center": ["n"]}, 
 *         "update": {"n": ["n"]}, 
 *         "controls": {
 *           "n": {
 *             "ncols": null, 
 *             "control_type": "selector", 
 *             "raw": true, 
 *             "default": 0, 
 *             "label": null, 
 *             "nrows": null, 
 *             "subtype": "list", 
 *             "values": 10, "width": "", 
 *             "value_labels": ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]}}}, 
 *       "msg_type": "interact_prepare"}, 
 *     "header": {"msg_id": "4744042977225053037"}
 * }, {
 *   "parent_header": {
 *     "username": "", 
 *     "msg_id": "749529bd-bcfe-43a7-b660-2cea20df3f32", 
 *     "session": "7af9a99a-2a0d-438f-8576-5e0bd853501a"}, 
 *   "msg_type": "stream", "sequence": 1, "output_block": "8157151862156143292", 
 *   "content": {"data": "0\n", "name": "stdout"}, "header": {"msg_id": "7423072463567901439"}}, {"parent_header": {"username": "", "msg_id": "749529bd-bcfe-43a7-b660-2cea20df3f32", "session": "7af9a99a-2a0d-438f-8576-5e0bd853501a"}, "msg_type": "execute_reply", "sequence": 2, "output_block": null, "content": {"status": "ok"}, "header": {"msg_id": "1900046197361249484"}}]})
 * 
 * 
 */

public class Interact extends CommandOutput {
	private final static String TAG = "Interact";
	
	private final String id;
	protected JSONObject controls;
	protected JSONArray layout;
	
	
	protected Interact(JSONObject json) throws JSONException {
		super(json);
		Log.i(TAG, "Created a new Interact!");
		JSONObject interact = json.getJSONObject("content").getJSONObject("data").getJSONObject("application/sage-interact");
		id = interact.getString("new_interact_id");
		controls = interact.getJSONObject("controls");
		layout = interact.getJSONArray("layout");
	}

	public long extendTimeOut() {
		return 60 * 1000;
	}
	
	public boolean isInteract() {
		return true;
	}
	
	public String getID() {
		return id;
	}
	
	public String toString() {
		return "Prepare interact id=" + getID();
	}
	
	public JSONObject getControls() {
		return controls;
	}
	
	public JSONArray getLayout() {
		return layout;
	}

}


/*

HTTP/1.1 200 OK
Server: nginx/1.0.11
Date: Tue, 10 Jan 2012 21:03:24 GMT
Content-Type: text/javascript; charset=utf-8
Connection: keep-alive
Content-Length: 1225

jQuery150664064213167876_1326208736019({"content": [{
"parent_header": {"username": "", "msg_id": "f0826b78-7aaf-4eea-a5ce-0b10d24c5888", "session": "184c2fb0-1a8d-4c07-aee6-df85183d9ac2"}, 
"msg_type": "extension", "sequence": 0, "output_block": null, "content": {"content": {"interact_id": "5455242645056671226", "layout": {"top_center": ["n"]}, "update": {"n": ["n"]}, "controls": {"n": {"control_type": "slider", "raw": true, "default": 0.0, "step": 0.40000000000000002, "label": null, "subtype": "continuous", "range": [0.0, 100.0], "display_value": true}}}, "msg_type": "interact_prepare"}, 
"header": {"msg_id": "8880643058896313398"}}, {
"parent_header": {"username": "", "msg_id": "f0826b78-7aaf-4eea-a5ce-0b10d24c5888", "session": "184c2fb0-1a8d-4c07-aee6-df85183d9ac2"}, 
"msg_type": "stream", "sequence": 1, "output_block": "5455242645056671226", "content": {"data": "0\n", "name": "stdout"}, "header": {"msg_id": "8823336185428654730"}}, {"parent_header": {"username": "", "msg_id": "f0826b78-7aaf-4eea-a5ce-0b10d24c5888", "session": "184c2fb0-1a8d-4c07-aee6-df85183d9ac2"}, "msg_type": "execute_reply", "sequence": 2, "output_block": null, "content": {"status": "ok"}, "header": {"msg_id": "1224514542068124881"}}]})


POST /eval?callback=jQuery150664064213167876_1326208736022 HTTP/1.1
Host: sagemath.org:5467
Connection: keep-alive
Content-Length: 368
Origin: http://sagemath.org:5467
x-requested-with: XMLHttpRequest
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.63 Safari/535.7
content-type: application/x-www-form-urlencoded
accept: text/javascript, application/javascript,  q=0.01
Referer: http://sagemath.org:5467/
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8,fr;q=0.6,de;q=0.4,ja;q=0.2
Accept-Charset: ISO-8859-1,utf-8;q=0.7,;q=0.3
Cookie: __utma=1.260350506.1306546516.1306612711.1306855420.3; HstCfa1579950=1313082432530; HstCmu1579950=1313082432530; c_ref_1579950=http:%2F%2Fboxen.math.washington.edu%2F; HstCla1579950=1314023965352; HstPn1579950=4; HstPt1579950=6; HstCnv1579950=2; HstCns1579950=2; __utma=138969649.2037720324.1307556884.1314205520.1326143310.11; __utmz=138969649.1314205520.10.6.utmcsr=en.wikipedia.org|utmccn=(referral)|utmcmd=referral|utmcct=/wiki/Sage_(mathematics_software)

message={"parent_header":{},"header":{"msg_id":"45724d5f-01f8-4d9c-9eb0-d877a8880db8","session":"184c2fb0-1a8d-4c07-aee6-df85183d9ac2"},"msg_type":"execute_request","content":{"code":"_update_interact('5455242645056671226',control_vals=dict(n=23.6,))","sage_mode":true}}


GET /output_poll?callback=jQuery150664064213167876_1326208736025&computation_id=184c2fb0-1a8d-4c07-aee6-df85183d9ac2&sequence=3&_=1326229408735 HTTP/1.1
Host: sagemath.org:5467
Connection: keep-alive
x-requested-with: XMLHttpRequest
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.63 Safari/535.7
accept: text/javascript, application/javascript,; q=0.01
Referer: http://sagemath.org:5467/
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8,fr;q=0.6,de;q=0.4,ja;q=0.2
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
Cookie: __utma=1.260350506.1306546516.1306612711.1306855420.3; HstCfa1579950=1313082432530; HstCmu1579950=1313082432530; c_ref_1579950=http%3A%2F%2Fboxen.math.washington.edu%2F; HstCla1579950=1314023965352; HstPn1579950=4; HstPt1579950=6; HstCnv1579950=2; HstCns1579950=2; __utma=138969649.2037720324.1307556884.1314205520.1326143310.11; __utmz=138969649.1314205520.10.6.utmcsr=en.wikipedia.org|utmccn=(referral)|utmcmd=referral|utmcct=/wiki/Sage_(mathematics_software)

HTTP/1.1 200 OK
Server: nginx/1.0.11
Date: Tue, 10 Jan 2012 21:03:29 GMT
Content-Type: text/javascript; charset=utf-8
Connection: keep-alive
Content-Length: 618

jQuery150664064213167876_1326208736025({"content": [{"parent_header": {"session": "184c2fb0-1a8d-4c07-aee6-df85183d9ac2", "msg_id": "45724d5f-01f8-4d9c-9eb0-d877a8880db8"}, "msg_type": "stream", "sequence": 3, "output_block": "5455242645056671226", "content": {"data": "23.6000000000000\n", "name": "stdout"}, "header": {"msg_id": "514409474887099253"}}, {"parent_header": {"session": "184c2fb0-1a8d-4c07-aee6-df85183d9ac2", "msg_id": "45724d5f-01f8-4d9c-9eb0-d877a8880db8"}, "msg_type": "execute_reply", "sequence": 4, "output_block": null, "content": {"status": "ok"}, "header": {"msg_id": "7283788905623826736"}}]})
 */