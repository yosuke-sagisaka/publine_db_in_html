<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class Article extends Model
{
    protected $table = 'm_publisher';
    protected $connection = '';


    public $timestamp = false;
    
    public function scopeGneder($query, $gender) {
        
        
        $users = DB::table('users')->get();
        
        
    }
    
    
    
    
}
