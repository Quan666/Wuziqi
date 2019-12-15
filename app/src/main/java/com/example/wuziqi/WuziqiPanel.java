package com.example.wuziqi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import static android.graphics.Paint.Align.CENTER;
import static android.graphics.Paint.Align.LEFT;

public class WuziqiPanel extends View {

    private int mPanelWidth;//棋盘宽
    private float mLineHeight;//棋盘行高
    private int MAX_LINE = 15;//最大行数

    private Paint mPaint = new Paint();
    private Paint mTextPaint = new Paint();

    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;

    //棋子与棋盘比例3/4
    private float ratioPieceOfLineHeight = 3 *1.0f /4;

    //白棋先手，当前轮到白棋
    private boolean mIsWhite = true;
    //棋子坐标集合
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();

    private boolean mIsGameOver;//游戏结束标记
    private boolean mIsWhiteWinner;//白子是赢家

    private int MAX_COUNT_IN_LINE = 5;//5子连线
    private int ADD_HEIGHT = 400;//文本提示高度
    private int MAX_ROW = 60;//文本提示行距


    //声音
    private SoundPool sPool;
    private int music_piece;//落子音效
    private int music_gameover;//游戏结束音效

    //联机
    private Online online;
    private boolean IsOnline = false;//当前是否为联机模式
    private boolean OnlineWait = false;//当前落子权限，true可以落子
    private boolean OnlineIsWhite = true;//联机模式下棋子颜色是否为白
    private boolean OnlineWaitAdd = true;//是否为等待对方加入对局状态

    private Ai ai;
    private boolean IsAI = false;//是否处于电脑对战模式

    //uiHandler在主线程中创建，所以自动绑定主线程
    //处理联机模式下消息接收线程反馈的信息
    //0：创建对战
    //1：落子信息
    //2：悔棋信息
    //3：结束游戏
    //4：求和
    //5：重新开始对战
    //6：认输  发送消息的为输家
    //7：发生错误
    //8：结束联机
    //9：等待对方加入联机，锁定棋盘
    //10：解锁棋盘
    // 信息的格式：(0||1||2||3||4),name,发送人id,x,y,IsWhite,IsGameOver$
    private Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    restart();
                    IsOnline = true;
                    OnlineIsWhite = online.getIsWhite();
                    if(!OnlineIsWhite){
                        OnlineWait = true;
                    }else {
                        OnlineWait = false;
                    }
                    break;
                case 1:
                    if(!IsOnline){
                        break;
                    }
                    if((Boolean)msg.obj==OnlineIsWhite){
                        OnlineIsWhite = !OnlineIsWhite;
                        OnlineWait = !OnlineWait;
                    }
                    if(OnlineWait){
                        if(OnlineIsWhite){
                            mBlackArray.add(new Point(msg.arg1,msg.arg2));
                        }
                        else {
                            mWhiteArray.add(new Point(msg.arg1,msg.arg2));
                        }
                        OnlineWait = false;
                        invalidate();
                        sPool.play(music_piece,1,1,0,0,1);
                    }
                    break;
                case 2:
                    if(mBlackArray.size()>0||mWhiteArray.size()>0){
                        if(OnlineIsWhite){
                            mBlackArray.remove(mBlackArray.size()-1);
                        }else {
                            mWhiteArray.remove(mWhiteArray.size()-1);
                        }
                        invalidate();
                        OnlineWait = !OnlineWait;
                    }

                    break;
                case 3:
                    mIsGameOver = true;
                    restart();
                    break;
                case 4:

                    break;
                case 5:
                    if((Boolean)msg.obj==OnlineIsWhite){
                        OnlineIsWhite = !OnlineIsWhite;
                        OnlineWait = !OnlineWait;
                    }
                    restart();
                    IsOnline = true;
                    break;
                case 6:
                    mIsGameOver = true;
                    mIsWhiteWinner = OnlineIsWhite?true:false;
                    sPool.play(music_gameover,1,1,0,0,1);
                    String text = mIsWhiteWinner ? "白棋胜利":"黑棋胜利";
                    Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    switch (msg.arg1){
                        case 0://服务器连接失败
                            Toast.makeText(getContext(),"联机结束！已关闭连接！",Toast.LENGTH_SHORT).show();
                            online.stopGame();
                            restart();
                            break;
                        case 1://加入联机失败
                            online.stopGame();
                            IsOnline = false;
                            Toast.makeText(getContext(),"加入联机失败！已关闭连接！",Toast.LENGTH_SHORT).show();
                            restart();

                            break;
                            default:
                                Toast.makeText(getContext(),"发生未知错误！！！",Toast.LENGTH_SHORT).show();
                                break;
                    }
                    invalidate();
                    break;
                case 8:
                    online.stopGame();
                    IsOnline = false;
                    Toast.makeText(getContext(),"对方断开联机！",Toast.LENGTH_SHORT).show();
                    restart();
                    break;
                case 9:
                    OnlineWaitAdd = true;
                    Toast.makeText(getContext(),"等待对方联机中！",Toast.LENGTH_SHORT).show();
                    break;
                case 10:
                    OnlineWaitAdd = false;
                    Toast.makeText(getContext(),"对方加入联机，开始对战！",Toast.LENGTH_SHORT).show();
                    invalidate();//重绘
                    break;
            }
        }
    };


    public WuziqiPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //setBackgroundColor(0x44ff0000);
        init(context);
    }

    private void init(Context context) {

        //画笔初始化
        mPaint.setColor(0xff000000);//画笔颜色
        mPaint.setAntiAlias(true);//
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint.setStrokeWidth(5);
        mTextPaint.setColor(0xff000000);//画笔颜色
        mTextPaint.setAntiAlias(true);//抗锯齿
        //mTextPaint.setDither(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(LEFT);
        mTextPaint.setTextSize(50);
        //棋子初始化
        mWhitePiece = BitmapFactory.decodeResource(getResources(),R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(),R.drawable.stone_b1);
        //音效初始化
        sPool = new SoundPool(1, AudioManager.STREAM_MUSIC,10);
        music_piece = sPool.load(context,R.raw.piece,1);
        music_gameover = sPool.load(context,R.raw.gameover,1);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取宽、高
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //获取宽高最小值
        int width = Math.min(widthSize,heightSize);
        //处理当宽或高为0时不显示
        if(widthMode == MeasureSpec.UNSPECIFIED){
            width = heightSize;
        }else if(heightMode == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }
        //设置一样的宽高，即正方形
        setMeasuredDimension(width,width+ADD_HEIGHT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = (mPanelWidth) *1.0f / MAX_LINE;

        int pieceWidth = (int) (mLineHeight * ratioPieceOfLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece,pieceWidth,pieceWidth,false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth,false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mIsGameOver) return false;
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN){//告诉父控件可以消化touch事件
            return true;
        }
        if(action == MotionEvent.ACTION_UP){
            //获取触摸坐标
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getValidPoint(x,y);
            //判断棋子已经存在
            if(mWhiteArray.contains(p) || mBlackArray.contains(p)){
                return true;
            }
            //判断棋盘下满没有
            if((mWhiteArray.size()+mBlackArray.size())>=(MAX_LINE*MAX_LINE)){
                restart();
                Toast.makeText(getContext(),"棋盘已满！自动和棋！",Toast.LENGTH_SHORT).show();
            }
            //判断是否在棋盘外
            if(p.y>=MAX_LINE){
                return true;
            }
            if(IsOnline){
                //联机模式处理
                if(OnlineWaitAdd){
                    return true;
                }
                //白棋先下
                if(OnlineWait){
                    Toast.makeText(getContext(),"等待对方落子！",Toast.LENGTH_SHORT).show();
                    return true;
                }else {
                    if(OnlineIsWhite){
                        mWhiteArray.add(p);
                    }else {
                        mBlackArray.add(p);
                    }
                    online.SendMsg(1,p.x,p.y);
                    OnlineWait = true;
                }
            }else {
                if(IsAI){
                    //电脑对战模式
                    mWhiteArray.add(p);
                        try {
                            Point point = ai.getNextStep(mWhiteArray,mBlackArray);
                            if(point!=null)mBlackArray.add(point);
                        }catch (Exception e){
                            Toast.makeText(getContext(),"出错！",Toast.LENGTH_SHORT).show();
                        }
                }else {
                    //面对面对战模式
                    if(mIsWhite){
                        mWhiteArray.add(p);
                    }else  {
                        mBlackArray.add(p);
                    }
                    mIsWhite = !mIsWhite;
                }
            }

            invalidate();//重绘
            sPool.play(music_piece,1,1,0,0,1);

            return true;
        }
        return super.onTouchEvent(event);
    }

    //获得正确的棋子落点坐标
    private Point getValidPoint(int x, int y) {
        return new Point((int)(x/mLineHeight),(int)(y/mLineHeight));
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawPieces(canvas);
        drawText(canvas);
        checkGameOver();
    }

    //游戏结束检测
    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);
        if(whiteWin || blackWin){
            mIsGameOver = true;
            mIsWhiteWinner = whiteWin;
            sPool.play(music_gameover,1,1,0,0,1);
            String text = mIsWhiteWinner ? "白棋胜利":"黑棋胜利";
            Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
        }
    }

    //五子连线检测
    private boolean checkFiveInLine(List<Point> points) {
        for(Point p : points){
            int x = p.x;
            int y = p.y;
            boolean win = checkHorizontal(x,y,points);
            if(win)return true;
            win = checkVertical(x,y,points);
            if(win)return true;
            win = checkLeftDiagonal(x,y,points);
            if(win)return true;
            win = checkRightDiagonal(x,y,points);
            if(win)return true;
        }
        return false;
    }

    //判断x，y处的棋子横向相邻的5个是否一致
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x-i,y))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x+i,y))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        return false;
    }
    //判断x，y处的棋子纵向相邻的5个是否一致
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x,y-i))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x,y+i))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        return false;
    }
    //判断x，y处的棋子左斜相邻的5个是否一致
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x-i,y+i))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x+i,y-i))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        return false;
    }
    //判断x，y处的棋子右斜相邻的5个是否一致
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x-i,y-i))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        for(int i = 1; i < MAX_COUNT_IN_LINE;i++){
            if(points.contains(new Point(x+i,y+i))){
                count++;
            }else {
                break;
            }
        }
        if(count == MAX_COUNT_IN_LINE) return true;
        return false;
    }

    //画棋子
    private void drawPieces(Canvas canvas) {
        //画白棋
        for(int i=0,n = mWhiteArray.size();i<n;i++){
            Point whitePoint = mWhiteArray.get(i);

            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x + (1 - ratioPieceOfLineHeight ) /2) * mLineHeight,
                    (whitePoint.y + (1 - ratioPieceOfLineHeight ) /2) * mLineHeight,null);
        }
        //画黑棋
        for(int i=0,n = mBlackArray.size();i<n;i++){
            Point blackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x + (1 - ratioPieceOfLineHeight ) /2) * mLineHeight,
                    (blackPoint.y + (1 - ratioPieceOfLineHeight ) /2) * mLineHeight,null);
        }
    }

    //画棋盘
    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;

        for(int i = 0;i < MAX_LINE ; i++){
            int startX = (int) (lineHeight / 2);
            int endX = (int) (w - lineHeight / 2);
            int y = (int) ((0.5 + i) * lineHeight);
            canvas.drawLine(startX , y , endX , y , mPaint);
            canvas.drawLine(y , startX , y , endX , mPaint);
        }
    }

    //画文字
    private void drawText(Canvas canvas){
        String text = "" ;
        int left = 50;//左边距
        if(IsOnline){
            String s = String.valueOf(OnlineIsWhite?"白色":"黑色" );
            text = "当前棋子颜色：" + s +"   联机对战";
            String log = String.valueOf(OnlineWait?"等待对方落子！":"请落子！" );

            canvas.drawText(text, left , canvas.getHeight()-(ADD_HEIGHT - MAX_ROW*1) , mTextPaint);
            canvas.drawText(log, left , canvas.getHeight()-(ADD_HEIGHT - MAX_ROW*2) , mTextPaint);
            String msglog_online = "Code："+online.getUser().getName()+"  id:"+online.getUser().getId() + "  " + String.valueOf(OnlineWaitAdd?"等待对方加入对局！":"" );
            canvas.drawText(msglog_online, left , canvas.getHeight()-(ADD_HEIGHT - MAX_ROW*3) , mTextPaint);
        }else {
            String ai = "";
            ai = IsAI?"电脑对战":"面对面对战";
            text = "当前棋子颜色：" + String.valueOf(mIsWhite?"白色":"黑色" ) + "   " + ai;
            canvas.drawText(text, left , canvas.getHeight()-(ADD_HEIGHT - MAX_ROW) , mTextPaint);
        }

    }



    //重来一局
    public void restart(){
        if(IsOnline){
            //online.SendMsg(8,0,0);
        }
        if(IsAI){
            mWhiteArray.clear();
            mBlackArray.clear();
            mIsGameOver = false;
            mIsWhiteWinner = false;
            mBlackArray.add(new Point(7,7));
            if(!mIsWhite){
                mIsWhite = false;
            }
            IsAI = true;
            invalidate();
        }
        IsOnline = false;
        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver = false;
        mIsWhiteWinner = false;
        invalidate();
    }
    //悔棋
    public void BackPiece(){
        if(mIsGameOver){
            restart();
        }else{
            if(mBlackArray.size()>0||mWhiteArray.size()>0) {
                if (IsAI) {
                    mBlackArray.remove(mBlackArray.size() - 1);
                    if (mWhiteArray.size() > 0) {
                        mWhiteArray.remove(mWhiteArray.size() - 1);
                    }
                    invalidate();
                } else {
                    if (!IsOnline) {
                        if (mIsWhite) {
                            mBlackArray.remove(mBlackArray.size() - 1);
                        } else {
                            mWhiteArray.remove(mWhiteArray.size() - 1);
                        }
                        invalidate();
                        mIsWhite = !mIsWhite;
                    }
                }
                }else {
                    if(OnlineWait){
                        if(!OnlineIsWhite){
                            online.SendMsg(2,mBlackArray.get(mBlackArray.size()-1).x,mBlackArray.get(mBlackArray.size()-1).y);
                            mBlackArray.remove(mBlackArray.size()-1);
                        }else {
                            online.SendMsg(2,mWhiteArray.get(mWhiteArray.size()-1).x,mWhiteArray.get(mWhiteArray.size()-1).y);
                            mWhiteArray.remove(mWhiteArray.size()-1);
                        }
                        invalidate();
                        OnlineWait = !OnlineWait;
                    }
                }
        }
    }



    //联机
    public void online(){
        if(IsOnline){
            //删除服务器上的数据5
            online.SendMsg(5,0,0);
            restart();
            IsOnline = true;
        }else{
                showInputOnlineCode();
        }

    }
    private void showInputOnlineCode() {
        /*@setView 装入一个EditView
         */
        final EditText editText = new EditText(getContext());
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(getContext());
        inputDialog.setTitle("输入数字联机码Code：").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            online = new Online("0",editText.getText().toString(),0,0,true,mIsGameOver,uiHandler);
                            online.start();
                            OnlineWait = true;
                            IsOnline = true;
                        }catch (Exception e){
                            Toast.makeText(getContext(),"联机失败："+e,Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    //认输
    public void GiveIn(){
        if(mIsGameOver)
            return;
        if(mBlackArray.size()<=0 && mWhiteArray.size()<=0)
            return;
        mIsGameOver = true;
        if(!IsOnline){
            mIsWhiteWinner = mIsWhite?false:true;
            sPool.play(music_gameover,1,1,0,0,1);
            String text = mIsWhiteWinner ? "白棋胜利":"黑棋胜利";
            Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
        }else {
            mIsWhiteWinner = OnlineIsWhite?false:true;
            sPool.play(music_gameover,1,1,0,0,1);
            String text = mIsWhiteWinner ? "白棋胜利":"黑棋胜利";
            Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
            online.SendMsg(6,0,0);
        }
    }


    //电脑
    public void computer(){
        if(!IsAI){
            ai = new Ai();
            restart();
            mBlackArray.add(new Point(7,7));
            if(!mIsWhite){
                mIsWhite = false;
            }
            IsAI = true;
            invalidate();
        }else {
            IsAI = false;
            mIsWhite = true;
            restart();;
            invalidate();
        }

    }
    //结束联机{
    public void stopOnline(){
        if(IsOnline){
            online.SendMsg(8,0,0);
            online.stopGame();
            IsOnline = false;
            restart();
        }else return;
    }














    //View的存储与恢复
    //View一定要写id！不然没有效果
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAME_OVER = "instace_game_over";
    private static final String INSTANCE_WHITE_ARRAY = "instace_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instace_black_array";
    @Nullable
    //存储
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER,mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY,mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY,mBlackArray);
        return bundle;
    }
    //恢复
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
        }
        super.onRestoreInstanceState(state);
    }

}
