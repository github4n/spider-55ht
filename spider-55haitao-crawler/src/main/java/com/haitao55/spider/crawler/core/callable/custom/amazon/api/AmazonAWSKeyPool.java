package com.haitao55.spider.crawler.core.callable.custom.amazon.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;

public class AmazonAWSKeyPool {
	
	private final Queue<AWSKey> queue = new ConcurrentLinkedQueue<AWSKey>();

	public AmazonAWSKeyPool() throws Exception {
		List<String> lines = new ArrayList<String>();
		/*//realtime
		lines.add("AKIAJU7VBBVJGJVG2YDA|aQmzzy0ZoiRMYvFxo5B8KDJnPgcmbmpTFT7OrIDg|yanyao-20");
		lines.add("AKIAJARRVRSBW6X5OVQQ|PN4lHg9QhuuSRiLk2EopnpcmOjWSS9uAmnIWC0R2|licheng03-20");
		lines.add("AKIAIP4ROPKO7HRNJ23A|PUHq/0DIan7gGg7oZEH28G2jisx0T4c7/f8VcIKa|taorui-20");
		lines.add("AKIAJMKAGA6ZJFS4S4JA|DLrPH5wQSEQbgWfw+URojKgpIEGqtlB3tcxwP7yW|dongmeng-20");
		lines.add("AKIAJWKRNNIZLIC3DETA|WPYOJwDMxF+W08/czOd35LyduPRrv+lW2A3Q/fCF|taosheng-20");
		lines.add("AKIAIU3TAMFJEAFSEHHQ|rAL9UKKu9ubFF40W1CZCoHEPQkPLdBHGdHF3iu87|junling-20");
		lines.add("AKIAI2RGPRDUIJD263BA|jNy05tXySl5A7dbw63C0StK+8ryt0ZdISCyMxzxc|yuanyuan00-20");
		lines.add("AKIAIRFT3W3YOOMNNXOA|88+ZjmwlgG7tYkrSU5ppzDbjlXo+liVu5yrRNpyh|foods0b3-20");*/
		//crawler
		//lines.add("AKIAIUZRDDRHTAQKW3IA|t/BlA4patqXH8qzFZl5ApVn9Qgf7WAyGF4qIIw82|mai888-20");//X not registered
		lines.add("AKIAIYUIYKW4VNFDZHNA|KQZ6FqcDCIe67IMwC1/ZcvPta5daI2nizEbG3x0O|ceshige-20");
		lines.add("AKIAI2VC66XB3QPAVGXQ|debcsD3eyGDxjrhhrvdMwJRDVBwWtjrWExKffRPC|wulx-20");
		lines.add("AKIAIWLFJF545J52DBQA|2xsidKKgx2P9MDHXU52kqXuEbIvnK+4fZ8/nuI8T|gechongy-20");
		//lines.add("AKIAI2RGPRDUIJD263BA|Nj4aaW5oC8WzP2KpmsEId0fFkE83AKbQC4grnmS/|fujie-20");//X signature error
		lines.add("AKIAIDGHHU5ME6IJIBSQ|yAkDAA9DdN7t19IcpUi6lxKjtvh7UOKhSrFnF0Sq|bmzach");
		//lines.add("AKIAIEB2PXRU52I4IDKA|1YIoXrCaC7yw67oyTDlYqvAQBhFr/57j/SG8CC54|weibo");//X not registered
		lines.add("AKIAJIS7LHAJ5KOG2SJA|uByKxeABo34wQwrcEG5xqaEGM2hvyJAH2A8PtCkp|liheng1-20");
		lines.add("AKIAJMMGMGCBYTP2LZ2A|+Kxc7DJ0f9v7EB9uzyyTtcMQ0OutZk6oscZfo100|nvfeng-20");
		lines.add("AKIAIZXHVXC2HFVDFNOQ|LmEiyWuow0IPF3U2BMvEjE4y3LP8u4HH/U9bdP65|fanyu09-20");
		lines.add("AKIAJXDQJUEN66M4SXAQ|g4NRHWPEKaRrMe5fyz4hA+4r8uy77D9blCpCyfuq|wuxi07-20");
		lines.add("AKIAIRODRHYBLMFMH7EA|hjBIgxCWXmYY/1PeobFHnuLdqijtR5g+q4AwbtGB|luwang-20");
		lines.add("AKIAIQBQPCUOGJZC5ZAQ|fOFatWQQhE5CaFBAxWwMVx3ouGYT0TnqXnlRtE2t|jianguo-20");
		lines.add("AKIAJ4LH3X4WNX6VVWGA|8O+tLpe1mMTOo5aQpY143d8Ltv5YFlW+XoaSNU7E|bofu04-20");
		lines.add("AKIAJ6AKYUXBVJC5L4DA|ubd2bHgZpIWa/MwLSCMTDAjd7QZ3j6dR6sN0qfvk|hanhong-20");
		//lines.add("AKIAI77X7X5JVEZ52ZCA|OErh4pPhz/T+6oOsfvym9bkYOzZ5N7Mh7oKEfWln|55haitao");
		for (String line : lines) {
			if (StringUtils.isBlank(line)) {
				break;
			}
			String[] key = StringUtils.split(line, "|");
			if (key.length > 0) {
				queue.offer(new AWSKey(key[0], key[1],key[2]));
			}

		}
	}
	
	public synchronized AWSKey pollKey(){
		AWSKey awsKey  = queue.poll();
		while (null == awsKey) {
			awsKey = queue.poll();
        }
		queue.offer(awsKey);
		return awsKey;
	}
	

}
