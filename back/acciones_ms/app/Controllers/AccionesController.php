<?php

namespace App\Controllers;

use App\Models\Accion;
use Illuminate\Database\Eloquent\Collection;

class AccionesController
{
    public function all(): Collection
    {
        return Accion::where('categoria', 'accion')
            ->orderBy('id', 'desc')
            ->get();
    }

    public function find_by_sprint(int $sprintId): Collection
    {
        return Accion::where('sprint_id', $sprintId)
            ->where('categoria', 'accion')
            ->orderBy('id', 'asc')
            ->get();
    }

    public function update_cumplimiento(int $id, bool $cumplida): ?Accion
    {
        $accion = Accion::where('id', $id)
            ->where('categoria', 'accion')
            ->first();

        if (empty($accion)) {
            return null;
        }

        $accion->cumplida = $cumplida;
        $accion->save();
        return $accion;
    }

    public function evaluar_lote(array $listaAcciones): array
    {
        $actualizadas = [];
        foreach ($listaAcciones as $item) {
            if (!isset($item['id']) || !isset($item['cumplida'])) {
                continue;
            }
            $accion = $this->update_cumplimiento((int) $item['id'], (bool) $item['cumplida']);
            if ($accion !== null) {
                $actualizadas[] = $accion;
            }
        }
        return $actualizadas;
    }
}