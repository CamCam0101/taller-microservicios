<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class RetroItem extends Model
{
    protected $table = 'retro_items';

    protected $casts = [
        'sprint_id' => 'integer',
        'cumplida'  => 'boolean',
    ];
}