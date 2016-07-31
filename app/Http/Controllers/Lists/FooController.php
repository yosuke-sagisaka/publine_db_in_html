<?php

namespace App\Http\Controllers\Lists;

use Illuminate\Http\Request;

use App\Http\Requests;
use App\Http\Controllers\Controller;

class FooController extends Controller
{
    //
    
    public function index($name='hoge') {
        return view('foo',['name'=>$name]);
    }
    
    public function create() {
        
    }
    
    public function store(Request $request) {
        
    }
    
    public function show($id) {
        
    }
    
    public function edit($id) {
        
    }
    
    public function update(Request $request,$id) {
        
    }
    
    public function destroy($id) {
        
    }
}
