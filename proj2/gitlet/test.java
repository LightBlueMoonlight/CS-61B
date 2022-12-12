package gitlet;

public class test {

    public static String isPalindrome(String[] str) {
            if (str.length==0){
                return "输入不存在公共前缀";
            }

            int length = Integer.MAX_VALUE;
            for (String s : str){
                if (s.length()<length){
                    length = s.length();
                }

            }
            if (length==0){
                return "输入不存在公共前缀";
            }
            boolean flg = false;
            int x = 0;
            for (;x<length;x++){
                String str1 = str[0].substring(0,x+1);
                for (String ss : str){
                    if (!ss.substring(0,x+1).equals(str1)){
                        flg=true;
                        break;
                    }
                }
                if (flg){
                    break;
                }
            }
            if (x==0){
                return "输入不存在公共前缀";
            }
            return str[0].substring(0,x);
    }

    public static void main(String[] args) {
        String[] str ={"init"};
        Main.main(str);

    }
}
