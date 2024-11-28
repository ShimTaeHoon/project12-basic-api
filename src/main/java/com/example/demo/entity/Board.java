package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int no;

    @Column(length = 100, nullable = false)
    String title;

    @Column(length = 1500, nullable = false)
    String content;

    // 자료형을 Member엔티티로 수정하면
    // 작성자 필드는 Member의 PK(아이디) 컬럼을 참조하게 됨
    // 따라서 @Column 컬럼의 정보가 필요없음!
//    @Column(length = 50, nullable = false)
    @ManyToOne // 1:N (1:N 또는 1:1)
    Member writer;
    
    @Column(length = 200, nullable = true)
	String imgPath; // 파일 이름

}
