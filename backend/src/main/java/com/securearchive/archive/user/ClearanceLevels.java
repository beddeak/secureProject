package com.securearchive.archive.user;

public final class ClearanceLevels {
    
    private ClearanceLevels() {
    }
        public static String toDisplayName(Integer level) {
            if (level == null) {
                return "H-Null";
            }
            return switch (level) {
                case 0 -> "Level-0";
                        case 1 -> "Level-1";
                        case 2 -> "Level-2";
                        case 3 -> "Level-3";
                        case 4 -> "Level-4";
                        case 5 -> "Level-5";
                        case 6 -> "기지 이사관";
                        case 7 -> "AION 평의회";
                        case 8 -> "AION 평의회장";
                        case 9 -> "부관리자";
                        case 10 -> "관리자";
                        default -> "알 수 없는 등급";
                    };
                }     
        public static boolean canAccess(Integer userLevel, Integer requiredLevel) {
            int user = userLevel == null ? 0 : userLevel;
            int required = requiredLevel == null ? 0 : requiredLevel;


            return user >= required;
        }
}