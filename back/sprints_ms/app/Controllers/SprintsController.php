<?php

namespace App\Controllers;

use App\Models\Sprint;
use Illuminate\Database\Eloquent\Collection;

class SprintsController
{
    public function all(): Collection
    {
        return Sprint::orderBy('id', 'desc')->get();
    }

    public function find_sprint_by_id(int $id): ?Sprint
    {
        return Sprint::find($id);
    }

    public function find_latest(): ?Sprint
    {
        return Sprint::orderBy('id', 'desc')->first();
    }

    public function create(array $data): Sprint
    {
        $sprint = new Sprint();
        $sprint->nombre = $data['nombre'];
        $sprint->fecha_inicio = $data['fecha_inicio'];
        $sprint->fecha_fin = $data['fecha_fin'];
        $sprint->save();
        return $sprint;
    }

    public function update(int $id, array $data): ?Sprint
    {
        $sprint = Sprint::find($id);
        if (empty($sprint)) {
            return null;
        }
        $sprint->nombre = $data['nombre'] ?? $sprint->nombre;
        $sprint->fecha_inicio = $data['fecha_inicio'] ?? $sprint->fecha_inicio;
        $sprint->fecha_fin = $data['fecha_fin'] ?? $sprint->fecha_fin;
        $sprint->save();
        return $sprint;
    }

    public function delete(int $id): ?Sprint
    {
        $sprint = Sprint::find($id);
        if (empty($sprint)) {
            return null;
        }
        $sprint->delete();
        return $sprint;
    }
}