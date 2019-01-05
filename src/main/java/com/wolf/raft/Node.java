package com.wolf.raft;

/**
 * Description:
 * <br/> Created on 12/29/2018
 *
 * @author 李超
 * @since 1.0.0
 */
public class Node {

    private Node voteFor;
    private int term;
    private String url;
    private State state = State.FOLLOW;

    public Node() {
    }

    public Node(String url) {
        this.url = url;
    }

    public void incrTerm() {
        term++;
    }

    public Node getVoteFor() {
        return voteFor;
    }

    public void setVoteFor(Node voteFor) {
        this.voteFor = voteFor;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}

