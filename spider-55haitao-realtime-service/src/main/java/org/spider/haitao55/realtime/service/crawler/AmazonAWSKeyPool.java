package org.spider.haitao55.realtime.service.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AWSKey;

public class AmazonAWSKeyPool {
	
	private final Queue<AWSKey> queue = new ConcurrentLinkedQueue<AWSKey>();

	public AmazonAWSKeyPool() throws Exception {
		List<String> lines = new ArrayList<String>();
		
		/*lines.add("AKIAIUZRDDRHTAQKW3IA|t/BlA4patqXH8qzFZl5ApVn9Qgf7WAyGF4qIIw82|mai888-20");
		lines.add("AKIAIYUIYKW4VNFDZHNA|KQZ6FqcDCIe67IMwC1/ZcvPta5daI2nizEbG3x0O|ceshige-20");
		lines.add("AKIAI2VC66XB3QPAVGXQ|debcsD3eyGDxjrhhrvdMwJRDVBwWtjrWExKffRPC|wulx-20");
		lines.add("AKIAIWLFJF545J52DBQA|2xsidKKgx2P9MDHXU52kqXuEbIvnK+4fZ8/nuI8T|gechongy-20");
		lines.add("AKIAI2RGPRDUIJD263BA|Nj4aaW5oC8WzP2KpmsEId0fFkE83AKbQC4grnmS/|fujie-20");*/
		
		lines.add("AKIAI77X7X5JVEZ52ZCA|OErh4pPhz/T+6oOsfvym9bkYOzZ5N7Mh7oKEfWln|55haitao");
		
		
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
