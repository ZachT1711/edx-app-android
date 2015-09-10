package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {

    private boolean voteCountsEnabled;

    @ColorInt
    private final int edx_brand_primary_base;
    @ColorInt
    private final int edx_grayscale_neutral_light;
    @ColorInt
    private final int edx_grayscale_neutral_xx_light;
    @ColorInt
    private final int edx_grayscale_neutral_white;

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
        edx_brand_primary_base = context.getResources().getColor(R.color.edx_brand_primary_base);
        edx_grayscale_neutral_light = context.getResources().getColor(R.color.edx_grayscale_neutral_light);
        edx_grayscale_neutral_xx_light = context.getResources().getColor(R.color.edx_grayscale_neutral_xx_light);
        edx_grayscale_neutral_white = context.getResources().getColor(R.color.edx_grayscale_neutral_white);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;

        Iconify.IconValue iconValue = Iconify.IconValue.fa_comments;
        if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
            iconValue = discussionThread.isHasEndorsed() ?
                    Iconify.IconValue.fa_check_square_o : Iconify.IconValue.fa_question;
        }
        holder.discussionPostTypeIcon.setIcon(iconValue);

        String threadTitle = discussionThread.getTitle();
        holder.discussionPostTitle.setText(threadTitle);

        holder.discussionPostPinIcon.setVisibility(discussionThread.isPinned() ? View.VISIBLE : View.GONE);
        holder.discussionPostFollowIcon.setVisibility(discussionThread.isFollowing() ? View.VISIBLE : View.GONE);

        {
            String authorLabel = null;
            if (null != discussionThread.getAuthorLabel()) {
                switch (discussionThread.getAuthorLabel()) {
                    case STAFF: {
                        authorLabel = getContext().getString(R.string.discussion_priviledged_author_label_staff);
                        break;
                    }
                    case COMMUNITY_TA: {
                        authorLabel = getContext().getString(R.string.discussion_priviledged_author_label_ta);
                        break;
                    }
                }
            }
            if (authorLabel != null) {
                holder.discussionPostAuthor.setVisibility(View.VISIBLE);
                holder.discussionPostAuthor.setText(getContext().getString(R.string.discussion_priviledged_author_attribution, authorLabel));

            } else {
                holder.discussionPostAuthor.setVisibility(View.GONE);
            }
        }

        {
            final String text;
            final Iconify.IconValue icon;
            @ColorInt
            final int iconColor;
            if (voteCountsEnabled) {
                text = Integer.toString(discussionThread.getVoteCount());
                icon = Iconify.IconValue.fa_plus;
                iconColor = discussionThread.isVoted() ? edx_brand_primary_base : edx_grayscale_neutral_light;
            } else {
                text = Integer.toString(discussionThread.getCommentCount());
                icon = Iconify.IconValue.fa_comment;
                iconColor = discussionThread.getUnreadCommentCount() == 0 ? edx_grayscale_neutral_light : edx_brand_primary_base;

            }
            holder.discussionPostIndicatorTextView.setText(text);
            holder.discussionPostIndicatorTextView.setTextColor(iconColor);
            holder.discussionPostIndicatorIcon.setIcon(icon);
            holder.discussionPostIndicatorIcon.setIconColor(iconColor);
        }
        holder.discussionPostRow.setBackgroundColor(discussionThread.isRead() ? edx_grayscale_neutral_xx_light : edx_grayscale_neutral_white);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    public void setVoteCountsEnabled(boolean voteCountsEnabled) {
        this.voteCountsEnabled = voteCountsEnabled;
    }

    private static class ViewHolder extends BaseViewHolder {
        final RelativeLayout discussionPostRow;
        final IconView discussionPostTypeIcon;
        final TextView discussionPostTitle;
        final IconView discussionPostPinIcon;
        final IconView discussionPostFollowIcon;
        final TextView discussionPostAuthor;
        final TextView discussionPostIndicatorTextView;
        final IconView discussionPostIndicatorIcon;

        public ViewHolder(View convertView) {
            discussionPostRow = (RelativeLayout) convertView.findViewById(R.id.row_discussion_post_relative_layout);
            discussionPostTypeIcon = (IconView) convertView.findViewById(R.id.discussion_post_type_icon);
            discussionPostTitle = (TextView) convertView.findViewById(R.id.discussion_post_title);
            discussionPostPinIcon = (IconView) convertView.findViewById(R.id.discussion_post_pin_icon);
            discussionPostFollowIcon = (IconView) convertView.findViewById(R.id.discussion_post_following_icon);
            discussionPostAuthor = (TextView) convertView.findViewById(R.id.discussion_post_author);
            discussionPostIndicatorTextView = (TextView) convertView.findViewById(R.id.discussion_post_indicator_text);
            discussionPostIndicatorIcon = (IconView) convertView.findViewById(R.id.discussion_post_indicator_icon);
        }

    }
}
