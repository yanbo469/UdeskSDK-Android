package cn.udesk.activity;

import java.util.ArrayList;
import java.util.List;

import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskUtil;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.saas.sdk.R;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MessageAdatper extends BaseAdapter{
	private static final int[] layoutRes = {
			R.layout.udesk_chat_msg_item_txt_l, 
			R.layout.udesk_chat_msg_item_txt_r,
			R.layout.udesk_chat_msg_item_audiot_l,
			R.layout.udesk_chat_msg_item_audiot_r,
			R.layout.udesk_chat_msg_item_imgt_l,
			R.layout.udesk_chat_msg_item_imgt_r,
			R.layout.udesk_chat_msg_item_redirect
	};
	
	private static final int ILLEGAL = -1;
	private static final int MSG_TXT_L = 0;
	private static final int MSG_TXT_R = 1;
	private static final int MSG_AUDIO_L = 2;
	private static final int MSG_AUDIO_R = 3;
	private static final int MSG_IMG_L = 4;
	private static final int MSG_IMG_R = 5;
	private static final int MSG_REDIRECT = 6;
	
	private static final long SPACE_TIME = 3 * 60 * 1000;
	
	private Context mContext;
	private List<MessageInfo> list = new ArrayList<MessageInfo>();
	private DisplayImageOptions options;
	
	public MessageAdatper(Context context) {
		mContext = context;
		initDisplayOptions();
	}
	
	private void initDisplayOptions(){
		options = new DisplayImageOptions.Builder()  
         .showImageOnFail(R.drawable.udesk_defualt_failure)  
		 .showImageOnLoading(R.drawable.udesk_defalut_image_loading)
         .cacheInMemory(true)  
         .cacheOnDisk(true)  
         .bitmapConfig(Bitmap.Config.RGB_565)  
         .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
         .build();  
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	public List<MessageInfo> getList() {
		return list;
	}

	@Override
	public int getItemViewType(int position) {
		MessageInfo message = getItem(position);
		if(message == null){
			return ILLEGAL;
		}
		switch (UdeskConst.parseTypeForMessage(message.getMsgtype())) {
		case UdeskConst.ChatMsgTypeInt.TYPE_IMAGE:
			if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
				return MSG_IMG_L ; 
			}else{
				return MSG_IMG_R ; 
			}
		case UdeskConst.ChatMsgTypeInt.TYPE_TEXT:

			if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
				return MSG_TXT_L ;
			}else{
				return MSG_TXT_R ;
			}
		case UdeskConst.ChatMsgTypeInt.TYPE_AUDIO:
			if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
				return MSG_AUDIO_L ; 
			}else{
				return MSG_AUDIO_R; 
			}
		case UdeskConst.ChatMsgTypeInt.TYPE_REDIRECT:
			return MSG_REDIRECT;

		default:
			return ILLEGAL;
		}
	
	}

	@Override
	public int getViewTypeCount() {
		if (layoutRes.length > 0) {
			return layoutRes.length;
		}
		return super.getViewTypeCount();
	}

	
	/**
	 * 添加新的聊天记录
	 * 
	 * @param message
	 */
	public void addItem(MessageInfo message) {
		if (message == null) {
			return;
		}
		list.add(message);
	}
	
	public void addItems(List<MessageInfo>  messages){
		if(messages == null){
			return;
		}
		list.clear();
		list = messages;
		notifyDataSetChanged();
	}
	
	public void listAddItems(List<MessageInfo>  messages){
		if(messages == null){
			return;
		}
		List<MessageInfo>  tempMsgs = messages;
		tempMsgs.addAll(list);
		list.clear();
		list = tempMsgs;
		notifyDataSetChanged();
	}

	@Override
	public MessageInfo getItem(int position) {
		if (position < 0 || position >= list.size()) {
			return null;
		}
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MessageInfo msgInfo = getItem(position);
		if(msgInfo != null){
			int itemType = getItemViewType(position);
			convertView = initView(convertView, itemType, position,msgInfo);
			BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
			tryShowTime(position, holder, msgInfo);
			holder.setMessage(msgInfo);
			holder.initHead(itemType);
			holder.showStatusOrProgressBar();
			holder.bind(mContext);
		}
		return convertView;
	}

	private View initView(View convertView, int itemType, int position, final MessageInfo msgInfo) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					layoutRes[itemType], null);

			switch (itemType) {
			case MSG_TXT_L:
			case MSG_TXT_R: {
				TxtViewHolder holder = new TxtViewHolder();
				initItemNormalView(convertView, holder, itemType, position);
				holder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
				convertView.setTag(holder);
				break;
			}
			case MSG_AUDIO_L:
			case MSG_AUDIO_R: {
				AudioViewHolder holder = new AudioViewHolder();
				initItemNormalView(convertView, holder, itemType, position);
				holder.tvDuration = (TextView) convertView
						.findViewById(R.id.udesk_im_item_record_duration);
				holder.record_item_content = convertView.findViewById(R.id.udesk_im_record_item_content);
				holder.record_play = (ImageView) convertView.findViewById(R.id.udesk_im_item_record_play);
				convertView.setTag(holder);
				break;
			}
			case MSG_IMG_L:
			case MSG_IMG_R: {
				ImgViewHolder holder = new ImgViewHolder();
				initItemNormalView(convertView, holder, itemType, position);
				holder.imgView = (ImageView) convertView.findViewById(R.id.udesk_im_image);
				convertView.setTag(holder);
				break;
			}
			case MSG_REDIRECT:
				RedirectViewHolder holder = new RedirectViewHolder();
				initItemNormalView(convertView, holder, itemType, position);
				holder.redirectMsg = (TextView) convertView.findViewById(R.id.udesk_redirect_msg);
				convertView.setTag(holder);
				break;

			}
		}
		return convertView;
	}
	
	
	abstract class BaseViewHolder {
		public ImageView ivHeader, ivStatus;
		public TextView tvTime;
		public ProgressBar pbWait;
		public MessageInfo message;
		public int itemType;
		public boolean isLeft = false;
		public void setMessage(MessageInfo message) {
			this.message = message;
		}
		
		public int getItemType() {
			return itemType;
		}
		public MessageInfo getMessage() {
			return message;
		}
		void initHead(int itemType){
			this.itemType = itemType;
			switch (itemType) {
			case MSG_TXT_R:
			case MSG_AUDIO_R:
			case MSG_IMG_R:
				ivHeader.setImageResource(R.drawable.udesk_im_default_user_avatar);
				this.isLeft = false;
				break;
			case MSG_TXT_L:
			case MSG_AUDIO_L:
			case MSG_IMG_L:
				this.isLeft = true;
				ivHeader.setImageResource(R.drawable.udesk_im_default_agent_avatar);
				break;
			default:
				break;
			}
			
		}
		
		public void  showStatusOrProgressBar(){
			if(itemType == MSG_TXT_L 
					|| itemType == MSG_AUDIO_L
					|| itemType == MSG_IMG_L
					|| itemType == MSG_REDIRECT){
				
				ivStatus.setVisibility(View.GONE);
			}else{
				 changeUiState(message.getSendFlag());
			}
		}
		
		public void changeUiState(int state){
			if( state==UdeskConst.SendFlag.RESULT_SUCCESS   ){
				ivStatus.setVisibility(View.GONE);
				pbWait.setVisibility(View.GONE);
	        }else {
	            if(state==UdeskConst.SendFlag.RESULT_RETRY || state==UdeskConst.SendFlag.RESULT_SEND){
	            	ivStatus.setVisibility(View.GONE);
	            	pbWait.setVisibility(View.VISIBLE);
	            }else if(state==UdeskConst.SendFlag.RESULT_FAIL){
	            	ivStatus.setVisibility(View.VISIBLE);
	            	pbWait.setVisibility(View.GONE);
	            }
	        }
		}
		abstract void bind(Context context);
		
	}
	
	class TxtViewHolder extends BaseViewHolder {
		public TextView tvMsg;
		@Override
		void bind(Context context) {
			tvMsg.setText(UDEmojiAdapter.replaceEmoji(context, message.getMsgContent(),
					(int) tvMsg.getTextSize()));
			
			tvMsg.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					((UdeskChatActivity)mContext).handleText(message, v);
					return false;
				}
			});
			
			ivStatus.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((UdeskChatActivity)mContext).retrySendMsg(message);
				}
			});
		}
		
		View getSubjectContent() {
			return tvMsg;
		}
		
	}

	class AudioViewHolder extends BaseViewHolder {
		public TextView tvDuration;
		public View record_item_content;
		public ImageView record_play;
		public TextView getDurationView(){
			return tvDuration;
		}		
		
		@Override
		void bind(Context context) {
			checkPlayBgWhenBind();
			if(message.getDuration() > 0){
				char symbol=34; 
				tvDuration.setText(message.getDuration()+""  + String.valueOf(symbol));
			}
			record_item_content.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((UdeskChatActivity)mContext).clickRecordFile(message);
				}
			});
			ivStatus.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((UdeskChatActivity)mContext).retrySendMsg(message);
				}
			});
		}
		
		private void checkPlayBgWhenBind() {
			if (message.isPlaying) {
				resetAnimationAndStart();
			}else{
				record_play.setImageDrawable(mContext.getResources().getDrawable(
						isLeft ? R.drawable.udesk_im_record_left_default : R.drawable.udesk_im_record_right_default));
			}
		}

		private void resetAnimationAndStart() {
			record_play.setImageDrawable(mContext.getResources().getDrawable(
					isLeft ? R.drawable.udesk_im_record_play_left : R.drawable.udesk_im_record_play_right));
			Drawable playDrawable = record_play.getDrawable();
			if (playDrawable != null
					&& playDrawable instanceof AnimationDrawable) {
				((AnimationDrawable) playDrawable).start();
			}
		}
		
		// 判断开始播放

		public void startAnimationDrawable() {
			message.isPlaying = true;

			Drawable playDrawable = record_play.getDrawable();
			if (playDrawable instanceof AnimationDrawable) {
				((AnimationDrawable) playDrawable).start();
			} else {
				resetAnimationAndStart();
			}
		}

		// 关闭播放
		protected void endAnimationDrawable() {
			message.isPlaying = false;

			Drawable playDrawable = record_play.getDrawable();
			if (playDrawable != null
					&& playDrawable instanceof AnimationDrawable) {
				((AnimationDrawable) playDrawable).stop();
				((AnimationDrawable) playDrawable).selectDrawable(0);
			}
		}
	}

	public class ImgViewHolder extends BaseViewHolder {
		public ImageView imgView;

		@Override
		void bind(Context context) {
			ImageLoader.getInstance().displayImage(UdeskUtil.buildImageLoaderImgUrl(message), imgView,options);	
			imgView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((UdeskChatActivity)mContext).previewPhoto(message);
					
				}
			});
			ivStatus.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((UdeskChatActivity)mContext).retrySendMsg(message);
				}
			});
		}
	}
	
	public class RedirectViewHolder extends BaseViewHolder{
		public TextView redirectMsg;
		@Override
		void bind(Context context) {
			redirectMsg.setText(message.getMsgContent());
		}
		
	}
	
	private void initItemNormalView(View convertView, BaseViewHolder holder,
			int itemType, final int position) {
		holder.ivHeader = (ImageView) convertView.findViewById(R.id.udesk_iv_head);
		holder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
		holder.ivStatus = (ImageView) convertView.findViewById(R.id.udesk_iv_status);
		holder.pbWait = (ProgressBar) convertView.findViewById(R.id.udesk_im_wait);
	
	}
	
	private void tryShowTime(int position, BaseViewHolder holder,
			MessageInfo info) {
		if (needShowTime(position)) {
			holder.tvTime.setVisibility(View.VISIBLE);
			holder.tvTime.setText(UdeskUtils.formatLongTypeTimeToString(info.getTime()));
		} else {
			holder.tvTime.setVisibility(View.GONE);
		}
	}
	
	private boolean needShowTime(int position) {
		if (position == 0) {
			return true;
		} else if (position > 0) {
			MessageInfo preItem = getItem(position - 1);
			if (preItem != null) {
				try {
					MessageInfo item = getItem(position);
					long currTime = item.getTime();
					long preTime = preItem.getTime();
					return currTime - preTime > SPACE_TIME
							|| preTime - currTime > SPACE_TIME;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}
	


	public boolean changeImState(View convertView, String msgId, int state) {
		Object tag = convertView.getTag();
		if (tag != null && tag instanceof BaseViewHolder) {
			BaseViewHolder cache = (BaseViewHolder) tag;
			if (cache.message != null && msgId.equals(cache.message.getMsgId())) {
				cache.changeUiState(state);
				cache.message.setSendFlag(state);
				return true;
			}
		}

		return false;
	}
	public boolean changeVideoTime(View convertView, String msgId, int duration) {
		Object tag = convertView.getTag();
		if (tag != null && tag instanceof AudioViewHolder) {
			AudioViewHolder cache = (AudioViewHolder) tag;
			if (cache.message != null && msgId.equals(cache.message.getMsgId())) {
				char symbol=34; 
				cache.getDurationView().setText(duration + String.valueOf(symbol));
				cache.message.setDuration(duration);
				return true;
			}
		}
		
		return false;
	}
	
	
	public View getTextViewForContentItem(View contentView) {
		Object tag = contentView.getTag();
		if (tag != null && tag instanceof TxtViewHolder) {
			TxtViewHolder cache = (TxtViewHolder) tag;
			if (UdeskConst.parseTypeForMessage(cache.message.getMsgtype()) != UdeskConst.ChatMsgTypeInt.TYPE_TEXT) {
				throw new RuntimeException(" we need text type ");
			}
			return cache.getSubjectContent();
		}
		return null;
	}

	public void dispose() {
		if (mContext != null) {
			mContext = null;
		}
		if (list != null) {
			list.clear();
			list = null;
		}
	}

}
