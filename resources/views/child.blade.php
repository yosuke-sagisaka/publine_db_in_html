<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->

@extends('layout.master')

@section('title', 'Page Title1')


@section('sidebar')
    @@parent
    
    
    <p>ここはなんだ?</p>
    
@endsection

@section('content')
<font color="black">
    <p>本文ｺﾝﾃﾝﾂ</p>
</font>

@endsection
