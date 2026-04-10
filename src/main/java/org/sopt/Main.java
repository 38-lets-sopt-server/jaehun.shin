package org.sopt;

import java.util.List;
import java.util.Scanner;
import org.sopt.controller.PostController;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.request.UpdatePostRequest;
import org.sopt.dto.response.CommonResponse;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;

public class Main {
    public static void main(String[] args) {
        // 클라이언트는 Controller만 알면 돼요. Service도 Repository도 몰라도 돼요.
        PostController postController = new PostController();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== 에브리타임 게시판 ===");
            System.out.println("1. 게시글 작성");
            System.out.println("2. 전체 조회");
            System.out.println("3. 단건 조회");
            System.out.println("4. 게시글 수정");
            System.out.println("5. 게시글 삭제");
            System.out.println("0. 종료");
            System.out.print("메뉴 선택: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("제목: ");
                    String title = scanner.nextLine();
                    System.out.print("내용: ");
                    String content = scanner.nextLine();
                    System.out.print("작성자: ");
                    String author = scanner.nextLine();
                    // 클라이언트가 요청 객체를 만들어서 Controller에 전달
                    CommonResponse<CreatePostResponse> response = postController.createPost(
                            new CreatePostRequest(title, content, author)
                    );
                    System.out.println(response.getMessage());
                    break;

                case 2:
                    CommonResponse<List<PostResponse>> allPostsResponse = postController.getAllPosts();
                    List<PostResponse> posts = allPostsResponse.getData();

                    if (posts.isEmpty()) {
                        System.out.println("등록된 게시글이 없습니다.");
                    } else {
                        posts.forEach(p -> System.out.println(p + "\n---"));
                    }
                    break;

                case 3:
                    System.out.print("조회할 게시글 ID: ");
                    CommonResponse<PostResponse> getPostResponse = postController.getPost(scanner.nextLong());
                    scanner.nextLine();
                    if (getPostResponse.isSuccess()) {
                        System.out.println(getPostResponse.getData());
                    } else {
                        System.out.println(getPostResponse.getMessage());
                    }
                    break;

                case 4:
                    System.out.print("수정할 게시글 ID: ");
                    Long updateId = scanner.nextLong();
                    scanner.nextLine();
                    System.out.print("새 제목: ");
                    String newTitle = scanner.nextLine();
                    System.out.print("새 내용: ");
                    String newContent = scanner.nextLine();

                    CommonResponse<Void> updateResponse = postController.updatePost(
                            updateId,
                            new UpdatePostRequest(newTitle, newContent)
                    );
                    System.out.println(updateResponse.getMessage());
                    break;

                case 5:
                    System.out.print("삭제할 게시글 ID: ");
                    CommonResponse<Void> deleteResponse = postController.deletePost(scanner.nextLong());
                    scanner.nextLine();
                    System.out.println(deleteResponse.getMessage());
                    break;

                case 0:
                    running = false;
                    System.out.println("프로그램 종료");
                    break;

                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }

        scanner.close();
    }
}
