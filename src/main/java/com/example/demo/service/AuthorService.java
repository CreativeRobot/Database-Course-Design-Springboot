package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.SaveAuthorDTO;
import com.example.demo.entity.Author;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookAuthorRepository;
import com.example.demo.vo.AuthorVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 作者管理业务。 */
@Service
public class AuthorService {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookAuthorRepository bookAuthorRepository;

    @Transactional(readOnly = true)
    public PageVo<AuthorVo> listAuthors(String keyword, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<Author> authors = StringUtils.hasText(keyword)
                ? authorRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                : authorRepository.findAll(pageable);
        return PageVo.of(authors.map(this::toVo));
    }

    @Transactional(readOnly = true)
    public AuthorVo getAuthor(Long authorId) {
        return toVo(getAuthorOrThrow(authorId));
    }

    @Transactional
    public AuthorVo createAuthor(SaveAuthorDTO dto) {
        validateRequest(dto);
        Author author = Author.builder()
                .name(dto.getName().trim())
                .country(trimToNull(dto.getCountry()))
                .introduction(trimToNull(dto.getIntroduction()))
                .build();
        return toVo(authorRepository.save(author));
    }

    @Transactional
    public AuthorVo updateAuthor(Long authorId, SaveAuthorDTO dto) {
        validateRequest(dto);
        Author author = getAuthorOrThrow(authorId);
        author.setName(dto.getName().trim());
        author.setCountry(trimToNull(dto.getCountry()));
        author.setIntroduction(trimToNull(dto.getIntroduction()));
        return toVo(authorRepository.save(author));
    }

    @Transactional
    public void deleteAuthor(Long authorId) {
        Author author = getAuthorOrThrow(authorId);
        if (bookAuthorRepository.existsByAuthor_Id(authorId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "该作者已关联图书，无法删除");
        }
        authorRepository.delete(author);
    }

    private Author getAuthorOrThrow(Long authorId) {
        if (authorId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "作者不能为空");
        }
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "作者不存在"));
    }

    private void validateRequest(SaveAuthorDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "作者姓名不能为空");
        }
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "页码必须从1开始，每页数量必须在1到" + MAX_PAGE_SIZE + "之间");
        }
        return PageRequest.of(page - 1, size,
                Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
    }

    private AuthorVo toVo(Author author) {
        AuthorVo vo = new AuthorVo();
        vo.setId(author.getId());
        vo.setName(author.getName());
        vo.setCountry(author.getCountry());
        vo.setIntroduction(author.getIntroduction());
        vo.setCreateTime(author.getCreateTime());
        vo.setUpdateTime(author.getUpdateTime());
        return vo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
