<?php

namespace App\Controllers;

use App\Models\RetroItem;
use Illuminate\Database\Eloquent\Collection;

class RetroItemsController
{
    public function all(): Collection
    {
        return RetroItem::orderBy('id', 'asc')->get();
    }

    public function find_by_sprint(int $sprintId): Collection
    {
        return RetroItem::where('sprint_id', $sprintId)->orderBy('id', 'asc')->get();
    }

    public function find_by_id(int $id): ?RetroItem
    {
        return RetroItem::find($id);
    }

    public function create(array $data): RetroItem
    {
        $item = new RetroItem();
        $item->sprint_id = (int) $data['sprint_id'];
        $categoria = $data['categoria'] ?? 'otro';
        $item->categoria = $categoria;
        $item->descripcion = $data['descripcion'];
        $item->fecha_revision = !empty($data['fecha_revision']) ? $data['fecha_revision'] : null;

        // Regla de BD: solo 'accion' maneja estado cumplida (true/false); el resto va en null
        if ($categoria === 'accion') {
            $item->cumplida = isset($data['cumplida']) ? (bool) $data['cumplida'] : false;
        } else {
            $item->cumplida = null;
        }

        $item->save();
        return $item;
    }

    public function update(int $id, array $data): ?RetroItem
    {
        $item = RetroItem::find($id);
        if (empty($item)) {
            return null;
        }

        $nuevaCategoria = $data['categoria'] ?? $item->categoria;
        $item->categoria = $nuevaCategoria;
        $item->descripcion = $data['descripcion'] ?? $item->descripcion;

        if (array_key_exists('fecha_revision', $data)) {
            $item->fecha_revision = !empty($data['fecha_revision']) ? $data['fecha_revision'] : null;
        }

        if ($nuevaCategoria === 'accion') {
            if (array_key_exists('cumplida', $data) && $data['cumplida'] !== null) {
                $item->cumplida = (bool) $data['cumplida'];
            } elseif ($item->cumplida === null) {
                $item->cumplida = false;
            }
        } else {
            $item->cumplida = null;
        }

        $item->save();
        return $item;
    }

    public function delete(int $id): ?RetroItem
    {
        $item = RetroItem::find($id);
        if (empty($item)) {
            return null;
        }
        $item->delete();
        return $item;
    }
}