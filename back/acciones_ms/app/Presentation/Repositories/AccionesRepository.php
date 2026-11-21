<?php

namespace App\Presentation\Repositories;

use App\Controllers\AccionesController;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class AccionesRepository
{
    public function get_all_acciones(Request $req, Response $resp, $args): Response
    {
        $controller = new AccionesController();
        $rows = $controller->all();
        if (count($rows) == 0) {
            return $resp->withStatus(204);
        }
        $resp->getBody()->write($rows->toJson());
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }

    public function get_acciones_by_sprint(Request $req, Response $resp, $args): Response
    {
        $sprintId = (int) $args['sprint_id'];
        $controller = new AccionesController();
        $rows = $controller->find_by_sprint($sprintId);
        if (count($rows) == 0) {
            return $resp->withStatus(204);
        }
        $resp->getBody()->write($rows->toJson());
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }

    public function update_cumplimiento(Request $req, Response $resp, $args): Response
    {
        $id = (int) $args['id'];
        $body = $req->getBody()->getContents();
        $data = json_decode($body, true);

        if (!isset($data['cumplida'])) {
            return $resp->withStatus(400);
        }

        $controller = new AccionesController();
        $row = $controller->update_cumplimiento($id, (bool) $data['cumplida']);
        if (empty($row)) {
            return $resp->withStatus(404);
        }

        $resp->getBody()->write($row->toJson());
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }

    public function evaluar_lote(Request $req, Response $resp, $args): Response
    {
        $body = $req->getBody()->getContents();
        $data = json_decode($body, true);

        if (empty($data['acciones']) || !is_array($data['acciones'])) {
            return $resp->withStatus(400);
        }

        $controller = new AccionesController();
        $actualizadas = $controller->evaluar_lote($data['acciones']);

        $resp->getBody()->write(json_encode($actualizadas));
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }
}