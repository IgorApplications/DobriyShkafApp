package com.iapp.iapp_messenger.lib.chess_engine;

@FunctionalInterface
public interface OnGettingMove {

    void onGetting(Move move, TypePiece typePiece);
}
