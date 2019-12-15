package com.example.wuziqi;



import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Ai {

    private List<Point> WhitePiece;
    private List<Point> BlackPiece;

    private int[][] chessBoard = new int[15][15];

    private boolean[][][] wins = new boolean[15][15][1000];// 赢法统计数组
    private int count = 0;// 赢法统计数组的计数器

    int[] myWin ;
    int[] airingWin ;
    int u;
    int v;

    public Ai(){
        // 初始化赢法统计数组
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                for(int k = 0;k < 1000; k++){
                    wins[i][j][k] = false;
                }
            }
        }



// 阳线纵向90°的赢法
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                for (int k = 0; k < 5; k++) {
                    wins[i][j + k][count] = true;
                }
                count++;
            }
        }

// 阳线横向0°的赢法
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++) {
                for (int k = 0; k < 5; k++) {
                    wins[j + k][i][count] = true;
                }
                count++;
            }
        }

// 阴线斜向135°的赢法
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                for (int k = 0; k < 5; k++) {
                    wins[i + k][j + k][count] = true;
                }
                count++;
            }
        }

// 阴线斜向45°的赢法
        for (int i = 0; i < 11; i++) {
            for (int j = 14; j > 3; j--) {
                for (int k = 0; k < 5; k++) {
                    wins[i + k][j - k][count] = true;
                }
                count++;
            }
        }
        myWin = new int[count];
        airingWin = new int[count];

        // 初始化赢法统计数组
        for (int i = 0; i < count; i++) {
            myWin[i] = 0;
            airingWin[i] = 0;
        }
        //棋盘初始化
        for(int i =0 ;i<15;i++){
            for(int j = 0;j<15;j++ ){
                chessBoard[i][j]=0;
            }
        }
    }

    //生成棋盘数组
    public void ListTochessBoard(){

        for(Point point:WhitePiece){
            chessBoard[point.x][point.y] = 1;
        }
        for(Point point:BlackPiece){
            chessBoard[point.x][point.y] = 2;
        }
    }

    public Point getNextStep(List<Point> WhitePiece,List<Point> BlackPiece){

        this.WhitePiece = WhitePiece;
        this.BlackPiece = BlackPiece;
        ListTochessBoard();
        //玩家白棋，电脑黑棋
        // 遍历赢法统计数组
        int i = WhitePiece.get(WhitePiece.size()-1).x;
        int j = WhitePiece.get(WhitePiece.size()-1).y;
        for (int k = 0; k < count; k ++) {
            if (wins[i][j][k]) {
                // 如果存在赢法,则玩家此赢法胜算+1(赢法为5胜取胜)
                myWin[k] ++;
                System.out.println(myWin[k]);
                // 如果存在赢法,则电脑此赢法胜算赋值为6(永远不等于5,永远无法在此处取胜)
                airingWin[k] = 6;
                // 玩家落子后,此处赢法数组凑够5,玩家取胜
                if (myWin[k] == 5) {

                    // 游戏结束
                    //玩家赢
                    return null;
                }
            }

        }
        airingGo();
        //System.out.println(u+"  "+v);
        return new Point(u,v);
    }


    //电脑
    public void airingGo(){


        int u = 0;              // 电脑预落子的x位置
        int v = 0;              // 电脑预落子的y位置
        int[][] myScore = new int[15][15];       // 玩家的分数
        int[][] airingScore = new int[15][15];   // 电脑的分数
        int max = 0;            // 最优位置的分数

        // 初始化分数的二维数组
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                myScore[i][j] = 0;
                airingScore[i][j] = 0;
            }
        }

        // 通过赢法统计数组为两个二维数组分别计分
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (chessBoard[i][j] == 0) {
                    for (int k = 0; k < count; k++) {
                        if (wins[i][j][k]) {
                            if (myWin[k] == 1) {
                                myScore[i][j] += 200;
                            } else if (myWin[k] == 2) {
                                myScore[i][j] += 400;
                            } else if (myWin[k] == 3) {
                                myScore[i][j] += 2000;
                            } else if (myWin[k] == 4) {
                                myScore[i][j] += 10000;
                            }
                            if (airingWin[k] == 1) {
                                airingScore[i][j] += 220;
                            } else if (airingWin[k] == 2) {
                                airingScore[i][j] += 420;
                            } else if (airingWin[k] == 3) {
                                airingScore[i][j] += 2100;
                            } else if (airingWin[k] == 4) {
                                airingScore[i][j] += 20000;
                            }
                        }
                    }

                    // 如果玩家(i,j)处比目前最优的分数大，则落子在(i,j)处
                    if (myScore[i][j] > max) {
                        max = myScore[i][j];
                        u = i;
                        v = j;
                    } else if (myScore[i][j] == max) {
                        // 如果玩家(i,j)处和目前最优分数一样大，则比较电脑在该位置和预落子的位置的分数
                        if (airingScore[i][j] > airingScore[u][v]) {
                            u = i;
                            v = j;
                        }
                    }

                    // 如果电脑(i,j)处比目前最优的分数大，则落子在(i,j)处
                    if (airingScore[i][j] > max) {
                        max  = airingScore[i][j];
                        u = i;
                        v = j;
                    } else if (airingScore[i][j] == max) {
                        // 如果电脑(i,j)处和目前最优分数一样大，则比较玩家在该位置和预落子的位置的分数
                        if (myScore[i][j] > myScore[u][v]) {
                            u = i;
                            v = j;
                        }
                    }
                }
            }
        }
        //返回电脑落子
        this.u=u;
        this.v=v;
        //System.out.println(u+" * "+v);
        chessBoard[u][v] = 2;

        for (int k = 0; k < count; k++) {
            if (wins[u][v][k]) {
                airingWin[k] ++;
                myWin[k] = 6;
                if (airingWin[k] == 5) {
                    //返回失败
                    //电脑赢
                    return;
                }
            }
        }


    }
}
