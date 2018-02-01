package com.haitao55.spider.crawler.core.callable.custom.amazon.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;

public class AmazonRealTimePriceAWSKeyPool {
	
	private final Queue<AWSKey> queue = new ConcurrentLinkedQueue<AWSKey>();

	public AmazonRealTimePriceAWSKeyPool() throws Exception {
		List<String> lines = new ArrayList<String>();
		lines.add("AKIAJU7VBBVJGJVG2YDA|aQmzzy0ZoiRMYvFxo5B8KDJnPgcmbmpTFT7OrIDg|yanyao-20");
		lines.add("AKIAJARRVRSBW6X5OVQQ|PN4lHg9QhuuSRiLk2EopnpcmOjWSS9uAmnIWC0R2|licheng03-20");
		lines.add("AKIAIP4ROPKO7HRNJ23A|PUHq/0DIan7gGg7oZEH28G2jisx0T4c7/f8VcIKa|taorui-20");
		lines.add("AKIAJMKAGA6ZJFS4S4JA|DLrPH5wQSEQbgWfw+URojKgpIEGqtlB3tcxwP7yW|dongmeng-20");
		lines.add("AKIAJWKRNNIZLIC3DETA|WPYOJwDMxF+W08/czOd35LyduPRrv+lW2A3Q/fCF|taosheng-20");
		lines.add("AKIAIU3TAMFJEAFSEHHQ|rAL9UKKu9ubFF40W1CZCoHEPQkPLdBHGdHF3iu87|junling-20");
		lines.add("AKIAI2RGPRDUIJD263BA|jNy05tXySl5A7dbw63C0StK+8ryt0ZdISCyMxzxc|yuanyuan00-20");
		lines.add("AKIAIRFT3W3YOOMNNXOA|88+ZjmwlgG7tYkrSU5ppzDbjlXo+liVu5yrRNpyh|foods0b3-20");
		
		lines.add("AKIAJD27Q5MXNG56U7NQ|zVjiUWR0OgLJgWMEh1xccx2SwFqy44b2EKwRRMf4|wangyongfuhai-20");
		lines.add("AKIAJBR3HK3JJHHFQDOQ|YldUN0/Kqa3DqSYLoBZXqjBAwgbqTCageE1SsMDM|heyu08-20");
		
		lines.add("AKIAID6KGON4O6FHAMTQ|l4hI4Ack3x6c44SAFEyqnXlsEWU3zrHTEJOSIj9a|yinweijianhai-20");
		lines.add("AKIAIFX5F4KRJ6XV77YA|gcOi4f66vMXyBRIPbmcJv6dUvpzZY4+VhgRdql57|yangminghuiha-20");
		lines.add("AKIAJWLNK7L5GQZ3EWAQ|CbxtPD86Iq8xf0B8SvelWLUPop/6TjDpScCQLGn3|wangyongfuh02-20");
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
