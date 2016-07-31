<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <meta charset="UTF-8">
        <title></title>
    </head>
    <body>
        <?php
        // put your code here
        ?>
        
        @section('header')
            @include('layouts.header')
        @show
        
        <form method="POST" action="/auth/login">
            {!! csrf_field() !!}
            
            <div>
                ユーザID
                <input type="email" name="enail" value="{{ old('email') }}">
            </div>
            
            <div>
                パスワード
                <input type="password" name="password" id="password">
            </div>
            
            <div>
                <button type="submit">ログイン</button>
            </div>
            
            
        </form>
        
        
        @section('footer')
            @include('layouts.footer')
        @show
    </body>
</html>
