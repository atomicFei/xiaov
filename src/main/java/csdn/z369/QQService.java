/*
 * Copyright (c) 2012-2018, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package csdn.z369;

import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class QQService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(QQService.class);

    private final Map<Long, Friend> QQ_FRD_INFOS = new ConcurrentHashMap<Long, Friend>();

    /**
     * QQ groups.
     * &lt;groupId, group&gt;
     */
    private final Map<Long, Group> QQ_GROUPS = new ConcurrentHashMap<Long, Group>();
    private final Map<String, Long> QQ_GROUPS_REVERSE = new ConcurrentHashMap<String, Long>();

    private final Map<Long, GroupInfo> QQ_GROUP_INFOS = new ConcurrentHashMap<Long, GroupInfo>();
    /**
     * The latest group ad time.
     * &lt;groupId, time&gt;
     */
    private final Map<Long, Long> GROUP_AD_TIME = new ConcurrentHashMap<Long, Long>();
    /**
     * QQ discusses.
     * &lt;discussId, discuss&gt;
     */
    private final Map<Long, Discuss> QQ_DISCUSSES = new ConcurrentHashMap<Long, Discuss>();
    /**
     * The latest discuss ad time.
     * &lt;discussId, time&gt;
     */
    private final Map<Long, Long> DISCUSS_AD_TIME = new ConcurrentHashMap<Long, Long>();

    /**
     * QQ client.
     */
    private SmartQQClient xiaoV  ;

    private boolean isDispacher =false;

    /**
     * Initializes QQ client.
     */
    public void initQQClient() {
        LOGGER.info("不能读取出文字以外的内容，包括自带表情。。。");
        LOGGER.info("开始初始化小薇");
        xiaoV = new SmartQQClient(new MessageCallback() {
            @Override
            public void onMessage(final Message message)  {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500 + RandomUtils.nextInt(0,1000));
                            if(!Config.focusFriend) return;
                            onQQFriendMessage(message);
                        } catch (final Exception e) {
                            LOGGER.log(Level.ERROR, "XiaoV on group message error", e);
                        }
                    }
                }) .start();
            }

            @Override
            public void onGroupMessage(final GroupMessage message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500 + RandomUtils.nextInt(0,1000));
                            if(!Config.focusGroup) return;
                            onQQGroupMessage(message);
                        } catch (final Exception e) {
                            LOGGER.log(Level.ERROR, "XiaoV on group message error", e);
                        }
                    }
                }) .start();
            }

            @Override
            public void onDiscussMessage(final DiscussMessage message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500 + RandomUtils.nextInt(0,1000));
                            if(!Config.focusDis) return;

                            onQQDiscussMessage(message);
                        } catch (final Exception e) {
                            LOGGER.log(Level.ERROR, "XiaoV on group message error", e);
                        }
                    }
                }).start();
            }
        });

        reloadFriendss();
        reloadGroups();
        reloadDiscusses();
        LOGGER.info("小薇初始化完毕");
        LOGGER.info("初始化完毕，接收消息。。。");
        if(StringUtils.isNotBlank(Config.msg2SendGroupName )){
            Long aLong = QQ_GROUPS_REVERSE.get(Config.msg2SendGroupName);
            if(aLong!=null){
                Config.msg2SendGroupId = aLong.longValue();
                isDispacher = true;
            }
        }
        if(!isDispacher){
            LOGGER.info("转发群名称为空或者没有找到，无法转发！");
        }
    }


    /**
     * Closes QQ client.
     */
    public void closeQQClient() {
        if (null == xiaoV) {
            return;
        }

        try {
            xiaoV.close();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Closes QQ client failed", e);
        }
    }


    /**
     * 获取群id对应群详情
     *
     * @param id 被查询的群id
     * @return 该群详情
     */
    private   GroupInfo getGroupInfoFromID(Long id) {
        if (!QQ_GROUP_INFOS.containsKey(id)) {
             QQ_GROUP_INFOS.put(id, xiaoV.getGroupInfo(QQ_GROUPS.get(id).getCode()));
        }
        return QQ_GROUP_INFOS.get(id);
    }

    /**
     * 获取群id对应群详情
     *
      * @return 该群详情
     */
    private   GroupUser getGroupUserInfoFromGroupUserID(Long groupId,Long groupUserId) {
        GroupInfo groupInfoFromID = getGroupInfoFromID(groupId);
        if(groupInfoFromID !=null){
            for (GroupUser groupUser : groupInfoFromID.getUsers()) {
                if(groupUser.getUin() == groupUserId){
                    return groupUser;
                }
            }
        }
        return null;
    }

    private void onQQFriendMessage(final Message message) {
        final String content = message.getContent();
        String frdName ="无记录";
        if( QQ_FRD_INFOS.get(message.getUserId()) !=null){
            frdName = QQ_FRD_INFOS.get(message.getUserId()).getNickname()+"("+QQ_FRD_INFOS.get(message.getUserId()).getMarkname()+")";
        }
//        xiaoV.sendMessageToFriend(message.getUserId(),"Received "+frdName+" message: " + message.getContent()+",from uin :"+message.getUserId());

        LOGGER.info("Received "+frdName+" message: " + message.getContent()+",from uin :"+message.getUserId());
    }

    private void onQQGroupMessage(final GroupMessage message) {
        final long groupId = message.getGroupId();

        final String content = message.getContent();
        final long userId =  message.getUserId();
        // Push to third system
        String qqMsg = content.replaceAll("\\[\"face\",[0-9]+\\]", "");
        if (StringUtils.isNotBlank(qqMsg)) {
            qqMsg = "<p>" + qqMsg + "</p>";
            GroupUser groupUserInfo= getGroupUserInfoFromGroupUserID(groupId, userId);
            String nickName ="【没有找到对应的群友信息】";
            if(groupUserInfo!=null){
                nickName = groupUserInfo.getNick();
            }
            if(Config.focusNamesLike!=null && Config.focusNamesLike.length>0 ){
                boolean canLog =false;
                for (String name : Config.focusNamesLike) {
                    if(nickName.indexOf(name)!=-1){
                        canLog = true;
                        break;
                    }
                }
               if(canLog) {
                    LOGGER.info("\r\n--------\r\n"+"group msg ->[" + QQ_GROUPS.get(groupId).getName() + "] " + nickName +" say: "+qqMsg +"\r\n--------\r\n");
                   if(isDispacher) xiaoV.sendMessageToGroup(Config.msg2SendGroupId,"[" + QQ_GROUPS.get(groupId).getName() + "] " + nickName +" say: "+qqMsg);
               }
                 return;
            }

            LOGGER.info("\r\n--------\r\n"+"group msg ->[" + QQ_GROUPS.get(groupId).getName() + "] " +  (groupUserInfo==null?"【没有找到对应的群友信息】":groupUserInfo.getNick())+""+" say: "+qqMsg+"\r\n--------\r\n");


         }

    }

    private void onQQDiscussMessage(final DiscussMessage message) {
        final long discussId = message.getDiscussId();

        final String content = message.getContent();
        final String userName = Long.toHexString(message.getUserId());
        // Push to third system
        String qqMsg = content.replaceAll("\\[\"face\",[0-9]+\\]", "");
        if (StringUtils.isNotBlank(qqMsg)) {
            qqMsg = "<p>" + qqMsg + "</p>";
         }

        String msg = "";
    /*    if (StringUtils.contains(content, XiaoVs.QQ_BOT_NAME)
                || (StringUtils.length(content) > 6
                && (StringUtils.contains(content, "?") || StringUtils.contains(content, "？") || StringUtils.contains(content, "问")))) {
            msg = answer(content, userName);
        }*/

        if (StringUtils.isBlank(msg)) {
            return;
        }

        if (RandomUtils.nextFloat() >= 0.9) {
            Long latestAdTime = DISCUSS_AD_TIME.get(discussId);
            if (null == latestAdTime) {
                latestAdTime = 0L;
            }

            final long now = System.currentTimeMillis();

            if (now - latestAdTime > 1000 * 60 * 30) {
                msg = msg + "。\n"  ;

                DISCUSS_AD_TIME.put(discussId, now);
            }
        }

//        sendMessageToDiscuss(discussId, msg);
    }



    private void reloadFriendss() {
        final  List<Friend> friendList = xiaoV.getFriendList();

        final StringBuilder msgBuilder = new StringBuilder();

        msgBuilder.append("Reloaded Friends: \n");
        for (final Friend f : friendList) {
            msgBuilder.append("    ").append(f.getMarkname()).append("-> ").append(f.getNickname()).append(": ").append(f.getUserId()).append("\n");
            QQ_FRD_INFOS.put(f.getUserId(),f);
        }

        LOGGER.log(Level.INFO, msgBuilder.toString());
    }
    private void reloadGroups() {
        final List<Group> groups = xiaoV.getGroupList();
        QQ_GROUPS.clear();
        GROUP_AD_TIME.clear();
        QQ_GROUPS_REVERSE.clear();
        final StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("Reloaded groups: \n");
        for (final Group g : groups) {
            QQ_GROUPS.put(g.getId(), g);
            GROUP_AD_TIME.put(g.getId(), 0L);
            QQ_GROUPS_REVERSE.put(g.getName(),g.getId());
            msgBuilder.append("    ").append(g.getName()).append(": ").append(g.getId()).append("\n");
        }

        LOGGER.log(Level.INFO, msgBuilder.toString());
    }

    private void reloadDiscusses() {
        final List<Discuss> discusses = xiaoV.getDiscussList();
        QQ_DISCUSSES.clear();
        DISCUSS_AD_TIME.clear();

        final StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("Reloaded discusses: \n");
        for (final Discuss d : discusses) {
            QQ_DISCUSSES.put(d.getId(), d);
            DISCUSS_AD_TIME.put(d.getId(), 0L);

            msgBuilder.append("    ").append(d.getName()).append(": ").append(d.getId()).append("\n");
        }

        LOGGER.log(Level.INFO, msgBuilder.toString());
    }
}
