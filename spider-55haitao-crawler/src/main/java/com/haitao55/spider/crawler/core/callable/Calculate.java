package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：完成加/减/乘/除四种简单的数学计算功能
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:33:56
 * @version 1.0
 */
public class Calculate extends AbstractCallable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	// 操作数1
	private String operand1;
	// 操作数2
	private String operand2;
	// 操作符
	private String operator;
	// 运算结果精度，也就是精确到小数点后面几位
	private int precision;

	@Override
	public void invoke(Context context) throws Exception {
		Double output = null;

		try {
			double op1 = Double.parseDouble(String.valueOf(context.get(this.getOperand1())));
			double op2 = Double.parseDouble(String.valueOf(context.get(this.getOperand2())));
			Operator oper = Operator.codeOf(this.getOperator());

			switch (oper) {
			case ADD:
				output = op1 + op2;
				break;
			case SUB:
				output = op1 - op2;
				break;
			case MUL:
				output = op1 * op2;
				break;
			case DIV:
				output = op1 / op2;
				break;
			case MOD:
				output = op1 % op2;
				break;
			default:
				output = null;
				break;
			}

			if (this.getPrecision() > 0) {
				output = output * Math.pow(10, this.getPrecision());
			}

			output = (double) (Math.round(output) / Math.pow(10, this.getPrecision()));

			setOutput(context, output);
		} catch (Exception e) {
			logger.error("Callable-Error-Calculate::operand1:{};operand2:{};operator:{};exception:{}", operand1,
					operand2, operator, e);
		}
	}

	public String getOperand1() {
		return operand1;
	}

	public void setOperand1(String operand1) {
		this.operand1 = operand1;
	}

	public String getOperand2() {
		return operand2;
	}

	public void setOperand2(String operand2) {
		this.operand2 = operand2;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	private enum Operator {
		ADD("add"), SUB("sub"), MUL("mul"), DIV("div"), MOD("mod");

		private String value;

		private Operator(String value) {
			this.value = value;
		}

		private static Operator codeOf(String value) {
			for (Operator operator : values()) {
				if (StringUtils.equals(operator.getValue(), value)) {
					return operator;
				}
			}

			return null;
		}

		private String getValue() {
			return this.value;
		}
	}
}