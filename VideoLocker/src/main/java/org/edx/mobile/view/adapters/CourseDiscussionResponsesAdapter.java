package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CourseDiscussionResponsesAdapter extends RecyclerView.Adapter {

    @Inject
    Context context;

    @Inject
    Router router;

    @Inject
    DiscussionAPI discussionAPI;

    private static final int ROW_POSITION_THREAD = 0;

    private DiscussionThread discussionThread;
    private List<DiscussionComment> discussionResponses = new ArrayList<>();

    static class RowType {
        static final int THREAD = 0;
        static final int RESPONSE = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RowType.THREAD) {
            View discussionThreadRow = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.discussion_responses_thread_row, parent, false);

            return new DiscussionThreadViewHolder(discussionThreadRow);
        }

        View discussionResponseRow = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.discussion_responses_response_row, parent, false);

        return new DiscussionResponseViewHolder(discussionResponseRow);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == ROW_POSITION_THREAD) {
            bindViewHolderToThreadRow((DiscussionThreadViewHolder) holder);
        } else {
            bindViewHolderToResponseRow((DiscussionResponseViewHolder) holder, position);
        }
    }

    private void bindViewHolderToThreadRow(final DiscussionThreadViewHolder holder) {
        holder.threadTitleTextView.setText(discussionThread.getTitle());
        holder.threadBodyTextView.setText(discussionThread.getRawBody());

        if (discussionThread.isPinned()) {
            holder.threadPinnedIconView.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> authorMap = new HashMap<>();
        authorMap.put("author", discussionThread.getAuthor());
        authorMap.put("created_at", DateUtil.formatPastDateRelativeToCurrentDate(discussionThread.getCreatedAt()));
        String authorLabel = discussionThread.getAuthorLabel() == null ? "" :
                discussionThread.getAuthorLabel().getReadableText(context);
        authorMap.put("author_label", authorLabel);

        CharSequence authorText = ResourceUtil.getFormattedString(R.string.discussion_responses_author, authorMap);

        holder.authorTextView.setText(authorText);
        holder.numberResponsesTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                R.plurals.number_responses_or_comments_responses_label, discussionThread.getCommentCount()));

        updateActionBarVoteCount(holder.actionBarViewHolder,
                discussionThread.isVoted(), discussionThread.getVoteCount());
        updateActionBarFollow(holder.actionBarViewHolder, discussionThread.isFollowing());
        updateActionBarReportFlag(holder.actionBarViewHolder, discussionThread.isAbuseFlagged());

        holder.actionBarViewHolder.voteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discussionAPI.voteThread(discussionThread, !discussionThread.isVoted(), new APICallback<DiscussionThread>() {
                    @Override
                    public void success(DiscussionThread discussionThread) {
                        setDiscussionThread(discussionThread);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(Exception e) {

                    }
                });
            }
        });

        holder.actionBarViewHolder.followLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discussionAPI.followThread(discussionThread, !discussionThread.isFollowing(), new APICallback<DiscussionThread>() {
                    @Override
                    public void success(DiscussionThread discussionThread) {
                        setDiscussionThread(discussionThread);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(Exception e) {

                    }
                });
            }
        });

        holder.actionBarViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                discussionAPI.flagThread(discussionThread, !discussionThread.isAbuseFlagged(), new APICallback<DiscussionThread>() {
                    @Override
                    public void success(DiscussionThread discussionThread) {
                        setDiscussionThread(discussionThread);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(Exception e) {

                    }
                });
            }
        });
    }

    private void bindViewHolderToResponseRow(final DiscussionResponseViewHolder holder, final int position) {
        final DiscussionComment comment = discussionResponses.get(position - 1); // Subtract 1 for the discussion thread row at position 0

        holder.responseCommentBodyTextView.setText(comment.getRawBody());

        holder.addCommentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionComments(context, comment);
            }
        });

        HashMap<String, String> authorMap = new HashMap<>();
        authorMap.put("author", comment.getAuthor());
        authorMap.put("created_at", DateUtil.formatPastDateRelativeToCurrentDate(discussionThread.getCreatedAt()));
        String authorLabel = comment.getAuthorLabel() == null ? "" :
                comment.getAuthorLabel().getReadableText(context);
        authorMap.put("author_label", authorLabel);

        CharSequence authorText = ResourceUtil.getFormattedString(R.string.discussion_responses_author, authorMap);

        holder.authorTextView.setText(authorText);

        if (comment.isEndorsed()) {
            holder.answerLayout.setVisibility(View.VISIBLE);
            holder.endorsedTextView.setVisibility(View.VISIBLE);

            String endorsedAt = DateUtil.formatPastDateRelativeToCurrentDate(comment.getEndorsedAt());
            String endorsedAuthor = comment.getEndorsedBy();
            String endorsedLabel = comment.getEndorsedByLabel() == null ? "" :
                    comment.getEndorsedByLabel().getReadableText(context);

            HashMap<String, String> endorsedAuthorMap = new HashMap<>();
            endorsedAuthorMap.put("endorsed_by_author", endorsedAuthor);
            endorsedAuthorMap.put("endorsed_at", endorsedAt);
            endorsedAuthorMap.put("endorsed_by_label", endorsedLabel);

            CharSequence endorsedByText = ResourceUtil.getFormattedString(
                    R.string.discussion_responses_endorsed_author, endorsedAuthorMap);

            holder.endorsedTextView.setText(endorsedByText);
        }

        updateActionBarVoteCount(holder.actionBarViewHolder, comment.isVoted(), comment.getVoteCount());
        updateActionBarReportFlag(holder.actionBarViewHolder, comment.isAbuseFlagged());
        holder.actionBarViewHolder.followLayout.setVisibility(View.INVISIBLE);

        holder.actionBarViewHolder.voteLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                discussionAPI.voteComment(comment, !comment.isVoted(), new APICallback<DiscussionComment>() {
                    @Override
                    public void success(DiscussionComment comment) {
                        discussionResponses.set(position - 1, comment);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(Exception e) {
                    }
                });
            }
        });

        holder.actionBarViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                discussionAPI.flagComment(comment, !comment.isAbuseFlagged(), new APICallback<DiscussionComment>() {
                    @Override
                    public void success(DiscussionComment comment) {
                        discussionResponses.set(position - 1, comment);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(Exception e) {

                    }
                });
            }
        });

        int numChildren = comment == null ? 0 : comment.getChildren().size();

        if (numChildren == 0) {
            holder.numberCommentsTextView.setText(context.getString(
                    R.string.number_responses_or_comments_add_comment_label));
        } else {
            holder.numberCommentsTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                    R.plurals.number_responses_or_comments_comments_label, numChildren));
        }

    }

    @Override
    public int getItemCount() {
        return (discussionThread == null) ? 0 : 1 + discussionResponses.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RowType.THREAD;
        }

        return RowType.RESPONSE;
    }

    public void setDiscussionThread(DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
    }

    public void setDiscussionResponses(List<DiscussionComment> discussionResponses) {
        this.discussionResponses = discussionResponses;
    }

    public static class DiscussionThreadViewHolder extends RecyclerView.ViewHolder {
        ETextView threadTitleTextView;
        ETextView threadBodyTextView;
        IconView threadPinnedIconView;
        ETextView authorTextView;
        ETextView numberResponsesTextView;
        ActionBarViewHolder actionBarViewHolder;

        public DiscussionThreadViewHolder(View itemView) {
            super(itemView);

            threadTitleTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_title_text_view);
            threadBodyTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_body_text_view);
            threadPinnedIconView = (IconView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_pinned_icon_view);
            authorTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_author_label);
            numberResponsesTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_number_responses_or_comments_text_view);

            actionBarViewHolder = new ActionBarViewHolder(itemView);
        }
    }

    public static class DiscussionResponseViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout addCommentLayout;
        ETextView responseCommentBodyTextView;
        ETextView numberCommentsTextView;
        RelativeLayout answerLayout;
        ETextView endorsedTextView;
        ETextView authorTextView;
        ActionBarViewHolder actionBarViewHolder;

        public DiscussionResponseViewHolder(View itemView) {
            super(itemView);

            answerLayout = (RelativeLayout) itemView.findViewById(R.id.discussion_responses_answer_layout);
            addCommentLayout = (RelativeLayout) itemView.findViewById(R.id.discussion_responses_comment_relative_layout);
            responseCommentBodyTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_comment_body_text_view);
            numberCommentsTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_number_responses_or_comments_text_view);
            endorsedTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_endorsed_text_view);
            authorTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_response_row_author_label);

            actionBarViewHolder = new ActionBarViewHolder(itemView);
        }
    }

    void updateActionBarVoteCount(ActionBarViewHolder holder, boolean isVoted, int voteCount) {
        CharSequence voteText = ResourceUtil.getFormattedStringForQuantity(
                R.plurals.discussion_responses_action_bar_vote_text, voteCount);
        holder.voteCountTextView.setText(voteText);

        int iconColor = isVoted ? R.color.edx_brand_primary_base : R.color.edx_grayscale_neutral_base;
        holder.voteIconView.setIconColor(context.getResources().getColor(iconColor));
    }

    void updateActionBarFollow(ActionBarViewHolder holder, boolean isFollowing) {
        int followStringResId = isFollowing ? R.string.discussion_responses_action_bar_unfollow_text :
                R.string.discussion_responses_action_bar_follow_text;
        holder.followTextView.setText(context.getString(followStringResId));

        int iconColor = isFollowing ? R.color.edx_brand_primary_base : R.color.edx_grayscale_neutral_base;
        holder.followIconView.setIconColor(context.getResources().getColor(iconColor));
    }

    void updateActionBarReportFlag(ActionBarViewHolder holder, boolean isReported) {
        int reportStringResId = isReported ? R.string.discussion_responses_reported_label :
                R.string.discussion_responses_report_label;
        holder.reportTextView.setText(context.getString(reportStringResId));

        int iconColor = isReported ? R.color.edx_brand_primary_base : R.color.edx_grayscale_neutral_base;
        holder.reportIconView.setIconColor(context.getResources().getColor(iconColor));
    }

}
