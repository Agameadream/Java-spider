package WeiboSpider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

//微博图片爬虫单线程版 爬取地址：https://m.weibo.cn/
public class Weibo{
    private String uid;
    private String uname;
    private String fans;
    private static int picNum=0;
    private static int page;
    private static String original_pic;
    private static String name;
    public Weibo(String uid,String uname,String fans){
        this.uid=uid;
        this.uname=uname;
        this.fans=fans;
    }

    public static void main(String[] args){
        name="半半子";
        getUserList(name);
        getUserAllPic("2830125342");

    }
    public static void getUserAllPic(String uid){
        picNum=0;
        String containerid="230413"+ uid;
        try{
            for(page=1;;page++){
                getUserPic("https://m.weibo.cn/api/container/getIndex?containerid="+containerid+"&page="+page);
                System.out.println("---------------------------------------------------------");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("图片数："+picNum);
            System.out.println("页数："+page);
            if(!(e instanceof NullPointerException)){
                System.out.println("程序异常！！！");
            }
        }
    }
    private static boolean getUserPic(String url) throws NullPointerException{
        System.out.println("-------目录-------");
        System.out.println(url);
        System.out.println("-------开始-------");
        String content= MyHttpClientUtils.getByParams(url,null,null);
        //使用火狐浏览器打开url，返回的json直观
        //通过JSONObject来一步步解析json结构
        //原创微博图片(已验证是原图)json->data->cards[]->mblog->pics[]->url
        //获得https://wx1.sinaimg.cn/orj360/93b2d7e8ly1gesa4ftug0j20hs0npdig.jpg
        //替换orj360为large
        //转载微博图片(只有第一张是原图)json->data->cards[]->mblog->retweeted_status->original_pic
        JSONObject jsonObject = JSONObject.parseObject(content);
        JSONObject data=jsonObject.getJSONObject("data");
        JSONArray cards=data.getJSONArray("cards");
        for(int i=0;i<cards.size();i++){
            JSONObject mblog=cards.getJSONObject(i).getJSONObject("mblog");
            JSONArray pics=mblog.getJSONArray("pics");
            if(pics==null)continue;
            for(int j=0;j<pics.size();j++){
                original_pic=pics.getJSONObject(j).getString("url").replaceFirst("orj360","large");
                if(original_pic!=null) MyHttpClientUtils.getPic(original_pic,"F:\\pics\\"+name+"\\原图"+(++picNum)+original_pic.substring(original_pic.length()-4));
                System.out.println(original_pic);
                System.out.println("图片数："+picNum);
                System.out.println("页数："+page);
            }
        }
        return true;
    }
    public static List<Weibo> getUserList(String uname){
        String url= null;
        try {
            //中文需要转码
            url = "https://m.weibo.cn/api/container/getIndex?containerid=100103type"+URLEncoder.encode("=3&q="+uname+"&t=0","UTF-8")+"&page_type=searchall";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String content= MyHttpClientUtils.getByParams(url,null,null);
        //获得用户列表 json->data->cards[1]->card_group[]->user->id/screen_name/followers_count
        JSONObject jsonObject = JSONObject.parseObject(content);
        JSONObject data=jsonObject.getJSONObject("data");
        JSONArray cards=data.getJSONArray("cards");
        if(cards.size()>1){
            JSONArray card_group=cards.getJSONObject(1).getJSONArray("card_group");
            List<Weibo> weiboList=new ArrayList<Weibo>();
            for(int i=0;i<card_group.size();i++){
                JSONObject user=card_group.getJSONObject(i).getJSONObject("user");
                Weibo weibo=new Weibo(user.getString("id"),user.getString("screen_name"),user.getString("followers_count"));
                weiboList.add(weibo);
                System.out.println("用户："+weibo.uname+"|粉丝数："+weibo.fans+"|id："+weibo.uid);
            }
            return weiboList;
        }
        return null;
    }
}
