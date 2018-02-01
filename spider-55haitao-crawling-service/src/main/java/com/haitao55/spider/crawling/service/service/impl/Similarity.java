package com.haitao55.spider.crawling.service.service.impl;
/**
 * 
  * @ClassName: Similarity
  * @Description: 字符串相似度
  * @author songsong.xu
  * @date 2017年4月1日 下午3:58:31
  *
 */
public class Similarity {
	
	public double similarPercent(String str1, String str2) {
		// int maxLenth = str1.Length > str2.Length ? str1.Length : str2.Length;
		int val = editDistance(str1, str2);
		return 1 - (double) val / Math.max(str1.length(), str2.length());
	}

	private int lowerOfThree(int first, int second, int third) {
		int min = Math.min(first, second);
		return Math.min(min, third);
	}

	private int editDistance(String str1, String str2) {
		int[][] Matrix;
		int n = str1.length();
		int m = str2.length();

		int temp = 0;
		char ch1;
		char ch2;
		int i = 0;
		int j = 0;
		if (n == 0) {
			return m;
		}
		if (m == 0) {

			return n;
		}
		Matrix = new int[n + 1][m + 1];

		for (i = 0; i <= n; i++) {
			// 初始化第一列
			Matrix[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			// 初始化第一行
			Matrix[0][j] = j;
		}

		for (i = 1; i <= n; i++) {
			ch1 = str1.charAt(i - 1);
			for (j = 1; j <= m; j++) {
				ch2 = str2.charAt(j - 1);
				if (ch1 == ch2) {
					temp = 0;
				} else {
					temp = 1;
				}
				Matrix[i][j] = lowerOfThree(Matrix[i - 1][j] + 1, Matrix[i][j - 1] + 1, Matrix[i - 1][j - 1] + temp);
			}
		}
		/*for (i = 0; i <= n; i++) {
			for (j = 0; j <= m; j++) {
				System.out.println(" {0} :" + Matrix[i][j]);
			}
			System.out.println("");
		}*/
		return Matrix[n][m];
	}
	
	public static void main(String[] args) {
		//String str1 = "i5rrrvan1";
		//String str2 = "ittvan2";
		String str1 = "wwwabc";
		String str2 = "wwwddabc";
		System.out.println("字符串1 {0}:" + str1);
		System.out.println("字符串2 {0}:" + str2);
		System.out.println("相似度 {0} %:" + new Similarity().similarPercent(str1, str2) * 100);

	}
}
