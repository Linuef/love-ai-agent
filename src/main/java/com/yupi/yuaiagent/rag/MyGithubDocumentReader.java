package com.yupi.yuaiagent.rag;

import com.alibaba.cloud.ai.parser.tika.TikaDocumentParser;
import com.alibaba.cloud.ai.reader.github.GitHubDocumentReader;
import com.alibaba.cloud.ai.reader.github.GitHubResource;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
@Slf4j
public class MyGithubDocumentReader {
    private String owner;
    private String repo;
    private String branch;
    private String path;
    private GitHub gitHub;
    private String githubToken;
    public MyGithubDocumentReader(GitHub github,String owner,String repo,String branch,String path){
        this.gitHub = github;
        this.owner = owner;
        this.repo = repo;
        this.branch = branch;
        this.path = path;
    }



    public List<Document> read(){
        // 创建 Tika Parser
        TikaDocumentParser tikaDocumentParser = new TikaDocumentParser();

        GitHubResource gitHubResource = GitHubResource.builder()
                .gitHub(gitHub)
                .owner(owner)
                .repo(repo)
                .branch(branch)
                .path(path)
                .build();
        GitHubDocumentReader reader = new GitHubDocumentReader(gitHubResource,tikaDocumentParser);
        List<Document> documents = reader.read();
        return documents;
    }
    public List<Document> readList(){
        // 创建 Tika Parser
        TikaDocumentParser tikaDocumentParser = new TikaDocumentParser();

        List<GitHubResource> gitHubResources = GitHubResource.builder()
                .gitHub(gitHub)
                .owner(owner)
                .repo(repo)
                .branch(branch)
                .path(path)
                .buildBatch();
        GitHubDocumentReader reader = new GitHubDocumentReader(gitHubResources,tikaDocumentParser);
        List<Document> documents = reader.read();
        return documents;
    }
    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private GitHub gitHub;
        private String owner;
        private String repo;
        private String branch;
        private String path;
        private String githubToken;
        public Builder(){

        }
        public Builder githubToken(String githubToken){
            this.githubToken = githubToken;
            return this;
        }
        public Builder owner(String owner){
            this.owner = owner;
            return this;
        }
        public Builder repo(String repo){
            this.repo = repo;
            return this;
        }
        public Builder branch(String branch){
            this.branch = branch;
            return this;
        }
        public Builder path(String path){
            this.path = path;
            return this;
        }
        public MyGithubDocumentReader build(){
            GitHub github = null;
            try {
                github = GitHub.connectUsingOAuth(githubToken);
            } catch (IOException e) {
                log.error("与github连接失败",e);
                throw new RuntimeException(e);
            }
            this.gitHub = github;
            return new MyGithubDocumentReader(this.gitHub,this.owner,this.repo,this.branch,this.path);
        }
    }
}
