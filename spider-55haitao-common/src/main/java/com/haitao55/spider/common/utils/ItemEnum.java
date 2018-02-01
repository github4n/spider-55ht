package com.haitao55.spider.common.utils;

public class ItemEnum {

	public enum Status {
		ONLINE("ONLINE"), OFFLINE("OFFLINE");
		
        private String name;

        private Status(String name) {
            this.name = name;
        }

        // 覆盖方法
        @Override
        public String toString() {
            return this.name;
        }
    }
	
	public enum DocType {
		INSERT("INSERT"), DELETE("DELETE"), NOT_SELECT_REQUIRED_PROPERTY("NOT_SELECT_REQUIRED_PROPERTY");
		
        private String name;

        private DocType(String name) {
            this.name = name;
        }

        // 覆盖方法
        @Override
        public String toString() {
            return this.name;
        }
    }
}
