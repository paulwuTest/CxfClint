package com.aisino.procotocl.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aisino.procotocl.outbean.Password;

/**
 * <p>
 * [验证电商平台传递的用户名和密码]
 * </p>
 * 
 * @author wuyong@aisino.com
 * @version 1.0 Created on Sep 8, 2013 1:44:32 PM
 */
public class PassWordCheck {

    private final static Logger log = LoggerFactory.getLogger(PassWordCheck.class);

    private static final String ALGORITHM = "MD5";
    private static final String CHARSET = "UTF-8";
    public static PassWordCheck passWordCheck = null;

    public static PassWordCheck instantiation() {
        if (passWordCheck == null) {
            passWordCheck = new PassWordCheck();
        }
        return passWordCheck;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        /**
         * MessageDigest md = MessageDigest.getInstance(ALGORITHM);
         * md.update(("7688118571" + "43442886").getBytes(CHARSET));
         * System.out.print(ProXml.encode(md.digest()));
         */
        Password mm = PassWordCheck.passWordCreate("88343512");
        System.out.println(mm.getSjm()+mm.getPass());
        System.out.println(checkPassWord(mm.getSjm()+mm.getPass(),"88343512"));
    }

    /**
     * <p>
     * 验证电商平台传递的用户名和密码是否符合规范
     * </p>
     *
     * @param dsptbm
     *            电商平台编号
     * @param passWord
     *            电商平台密码
     * @return boolean
     * @author: wuyong@aisino.com
     * @date: Created on Sep 8, 2013 1:41:23 PM
     */
    public static boolean checkPassWord(String passWord, String zch) {
        if (passWord.length() <= 10) {
            return false;
        }
        try {
            int length = 10;
            String random = passWord.substring(0, length);
            String Md5Str = passWord.substring(length, passWord.length());
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update((random + zch).getBytes(CHARSET));
            return Md5Str.equals(new String(ProXml.encode(md.digest())));
        } catch (Exception e) {
            log.error("未知：" + e);
            return false;
        }
    }

    /**
     * <p>
     * 生成24位密码
     * </p>
     *
     * @param dsptbm
     * @param zch
     * @return String
     * @author: wuyong@aisino.com
     * @date: Created on Sep 18, 2013 4:12:59 PM
     */
    public static Password passWordCreate(String zch) {
        Password password = new Password();
      //修改String串为空的判断
        if ( StringUtils.isEmpty(zch)) {
            return password;
        }
        try {
            StringBuffer bf = new StringBuffer();
            bf.append(getRdom(10));
            String sjm = bf.toString();
            bf.append(zch);
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(bf.toString().getBytes(CHARSET));
            password.setPass(new String(ProXml.encode(md.digest())));
            password.setSjm(sjm);
            return password;
        } catch (Exception e) {
            log.error("未知", e);
            return password;
        }
    }
    public static String getRdom(int ws) {
        Random r = new Random();
        String nums = Integer.toString(Math.abs(r.nextInt(Integer.MAX_VALUE)));
        if (nums.length() >= ws)
            return nums.substring(nums.length() - ws);
        else
            return StringUtils.leftPad(nums, ws, "0");
    }
}
