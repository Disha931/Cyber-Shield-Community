package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Fragment_Community extends Fragment {

    private static final String[] CATEGORIES = {"Scam Alert", "Question", "Tip"};

    private Spinner spinnerPostCategory;
    private EditText etPostText;
    private LinearLayout llCommunityFeed;
    private TextView tvNoPosts;

    private DatabaseReference postsRef;
    private ValueEventListener feedListener;

    private String currentUid;
    private String currentUserName = "User";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerPostCategory = view.findViewById(R.id.spinnerPostCategory);
        etPostText = view.findViewById(R.id.etPostText);
        llCommunityFeed = view.findViewById(R.id.llCommunityFeed);
        tvNoPosts = view.findViewById(R.id.tvNoPosts);
        CardView cardSubmitPost = view.findViewById(R.id.cardSubmitPost);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        spinnerPostCategory.setAdapter(adapter);

        postsRef = FirebaseDatabase.getInstance().getReference("CommunityPosts");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser != null ? currentUser.getUid() : null;

        loadCurrentUserName();

        cardSubmitPost.setOnClickListener(v -> submitPost());

        attachFeedListener();
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

    private void submitPost() {
        if (currentUid == null) {
            Toast.makeText(getContext(), "You must be logged in to post", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spinnerPostCategory.getSelectedItem().toString();
        String text = etPostText.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(getContext(), "Write something before posting", Toast.LENGTH_SHORT).show();
            return;
        }

        String postId = postsRef.push().getKey();
        if (postId == null) {
            Toast.makeText(getContext(), "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        CommunityPost post = new CommunityPost(
                currentUid, currentUserName, category, text,
                System.currentTimeMillis(), 0, 0, new HashMap<>()
        );

        postsRef.child(postId).setValue(post)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Posted!", Toast.LENGTH_SHORT).show();
                    etPostText.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void attachFeedListener() {
        feedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) return; // fragment view might be gone already

                llCommunityFeed.removeAllViews();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tvNoPosts.setVisibility(View.VISIBLE);
                    return;
                }

                tvNoPosts.setVisibility(View.GONE);

                for (DataSnapshot child : snapshot.getChildren()) {
                    String postId = child.getKey();
                    CommunityPost post = child.getValue(CommunityPost.class);
                    if (post != null && postId != null) {
                        addPostRow(postId, post);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // fail safely
            }
        };

        postsRef.addValueEventListener(feedListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postsRef != null && feedListener != null) {
            postsRef.removeEventListener(feedListener);
        }
    }

    private void addPostRow(String postId, CommunityPost post) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_community_post, llCommunityFeed, false);

        TextView tvCategory = row.findViewById(R.id.tvPostCategory);
        TextView tvAuthor = row.findViewById(R.id.tvPostAuthor);
        TextView tvText = row.findViewById(R.id.tvPostText);
        TextView tvDate = row.findViewById(R.id.tvPostDate);
        LinearLayout llLike = row.findViewById(R.id.llLike);
        ImageView ivLikeIcon = row.findViewById(R.id.ivLikeIcon);
        TextView tvLikeCount = row.findViewById(R.id.tvLikeCount);
        LinearLayout llComment = row.findViewById(R.id.llComment);
        TextView tvCommentCount = row.findViewById(R.id.tvCommentCount);

        tvCategory.setText(post.category);
        tvAuthor.setText(post.authorName);
        tvText.setText(post.text);
        tvCommentCount.setText(String.valueOf(post.commentCount));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(post.timestamp));

        boolean likedByMe = currentUid != null && post.likedBy != null && post.likedBy.containsKey(currentUid);
        tvLikeCount.setText(String.valueOf(post.likeCount));
        ivLikeIcon.setImageResource(likedByMe
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);

        llLike.setOnClickListener(v -> toggleLike(postId, post));

        llComment.setOnClickListener(v ->
                Toast.makeText(getContext(), "Comments coming soon", Toast.LENGTH_SHORT).show());

        llCommunityFeed.addView(row);
    }

    private void toggleLike(String postId, CommunityPost post) {
        if (currentUid == null) {
            Toast.makeText(getContext(), "You must be logged in to like a post", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference postRef = postsRef.child(postId);
        boolean alreadyLiked = post.likedBy != null && post.likedBy.containsKey(currentUid);

        if (alreadyLiked) {
            postRef.child("likedBy").child(currentUid).removeValue();
            postRef.child("likeCount").setValue(Math.max(0, post.likeCount - 1));
        } else {
            postRef.child("likedBy").child(currentUid).setValue(true);
            postRef.child("likeCount").setValue(post.likeCount + 1);
        }
        // no manual UI update needed here — attachFeedListener() will
        // automatically re-fire and redraw once Firebase confirms the change
    }

    // Data holder — Firebase converts this to/from a database node automatically
    public static class CommunityPost {
        public String authorUid;
        public String authorName;
        public String category;
        public String text;
        public long timestamp;
        public int likeCount;
        public int commentCount;
        public Map<String, Boolean> likedBy;

        public CommunityPost() {} // required by Firebase

        public CommunityPost(String authorUid, String authorName, String category, String text,
                             long timestamp, int likeCount, int commentCount, Map<String, Boolean> likedBy) {
            this.authorUid = authorUid;
            this.authorName = authorName;
            this.category = category;
            this.text = text;
            this.timestamp = timestamp;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.likedBy = likedBy;
        }
    }
}