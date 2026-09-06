package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Fragment_PostComments extends Fragment {

    private static final String ARG_POST_ID = "post_id";

    private String postId;
    private String currentUid;
    private String currentUserName = "User";

    private LinearLayout llCommentsList;
    private TextView tvNoComments;
    private EditText etCommentInput;

    private DatabaseReference postRef;
    private DatabaseReference commentsRef;

    // Standard factory method — this is how the caller passes in which post to show
    public static Fragment_PostComments newInstance(String postId) {
        Fragment_PostComments fragment = new Fragment_PostComments();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }

        llCommentsList = view.findViewById(R.id.llCommentsList);
        tvNoComments = view.findViewById(R.id.tvNoComments);
        etCommentInput = view.findViewById(R.id.etCommentInput);
        ImageView ivSendComment = view.findViewById(R.id.ivSendComment);

        if (postId == null) {
            Toast.makeText(getContext(), "Unable to load this post", Toast.LENGTH_SHORT).show();
            return;
        }

        postRef = FirebaseDatabase.getInstance().getReference("CommunityPosts").child(postId);
        commentsRef = postRef.child("comments");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser != null ? currentUser.getUid() : null;
        loadCurrentUserName();

        ivSendComment.setOnClickListener(v -> submitComment());

        loadComments();
    }

    private void loadCurrentUserName() {
        if (currentUid == null) return;

        FirebaseDatabase.getInstance().getReference("Users").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            currentUserName = name;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // keep default "User"
                    }
                });
    }

    private void submitComment() {
        if (currentUid == null) {
            Toast.makeText(getContext(), "You must be logged in to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etCommentInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(getContext(), "Write something before posting", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = commentsRef.push().getKey();
        if (commentId == null) return;

        CommentItem comment = new CommentItem(currentUserName, text, System.currentTimeMillis());

        commentsRef.child(commentId).setValue(comment)
                .addOnSuccessListener(unused -> {
                    etCommentInput.setText("");
                    incrementCommentCount();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to post comment", Toast.LENGTH_SHORT).show());
    }

    // 🔵 Uses a Firebase Transaction — safer than read-then-write for counters
    // that multiple users could update at nearly the same time.
    private void incrementCommentCount() {
        postRef.child("commentCount").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentCount = currentData.getValue(Integer.class);
                currentData.setValue((currentCount != null ? currentCount : 0) + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed,
                                   @Nullable DataSnapshot currentData) {
                // no UI action needed — the main Community feed listener will
                // pick up the updated count automatically next time it refreshes
            }
        });
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) return;

                llCommentsList.removeAllViews();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tvNoComments.setVisibility(View.VISIBLE);
                    return;
                }

                tvNoComments.setVisibility(View.GONE);

                for (DataSnapshot child : snapshot.getChildren()) {
                    CommentItem comment = child.getValue(CommentItem.class);
                    if (comment != null) {
                        addCommentRow(comment);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // fail safely
            }
        });
    }

    private void addCommentRow(CommentItem comment) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_comment, llCommentsList, false);

        TextView tvAuthor = row.findViewById(R.id.tvCommentAuthor);
        TextView tvText = row.findViewById(R.id.tvCommentText);
        TextView tvTime = row.findViewById(R.id.tvCommentTime);

        tvAuthor.setText(comment.authorName);
        tvText.setText(comment.text);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        tvTime.setText(sdf.format(comment.timestamp));

        llCommentsList.addView(row);
    }

    // Data holder — Firebase converts this to/from a database node automatically
    public static class CommentItem {
        public String authorName;
        public String text;
        public long timestamp;

        public CommentItem() {} // required by Firebase

        public CommentItem(String authorName, String text, long timestamp) {
            this.authorName = authorName;
            this.text = text;
            this.timestamp = timestamp;
        }
    }
}