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
    private int term = 1;
    private String ipPort;
    private State state = State.FOLLOW;

    public Node() {
    }

    public Node(String ipPort) {
        this.ipPort = ipPort;
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

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public String toString() {

        String voteForStr = null;
        if (null != voteFor) {
            if (this.equals(voteFor)) {
                voteForStr = ", voteFor{ self}";
            } else {
                voteForStr = ", voteFor{" +
                        " term=" + term +
                        ", ipPort='" + ipPort + '\'' +
                        ", state=" + state +
                        '}';
            }
        }

        return "Node{" +
                " term=" + term +
                ", ipPort='" + ipPort + '\'' +
                ", state=" + state + voteForStr +
                "}";
    }
}

